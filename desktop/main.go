package main

import (
	"sync"
	"time"
    "slices"
	"errors"
    "runtime"
    "strings"
    "path/filepath"
    "os"
    "fmt"
    "encoding/json"
    "net/http"
    "net/http/httputil"
	"database/sql"
	_ "github.com/mattn/go-sqlite3"
)

type Entry struct {
	Id int64
    Keyword string
    Pronounciation string
    Definition []string
    Usage []string
    Group []string
	LastLearned int64
}

func (entry *Entry) str() string {
	var builder strings.Builder
	builder.WriteString("{\n")
	builder.WriteString(fmt.Sprintf("    Id = %d,\n", entry.Id))
	builder.WriteString(fmt.Sprintf("    Keyword = %s,\n", entry.Keyword))
	builder.WriteString(fmt.Sprintf("    Pronounciation = %s,\n", entry.Pronounciation))
	builder.WriteString(fmt.Sprintf("    Definition = %s,\n", entry.Definition))
	builder.WriteString(fmt.Sprintf("    Usage = %s,\n", entry.Usage))
	builder.WriteString(fmt.Sprintf("    Group = %s,\n", entry.Group))
	builder.WriteString(fmt.Sprintf("    LastLearned = %d,\n", entry.LastLearned))
	builder.WriteString("}")
	return builder.String()
}

type Filter struct {
	Include []string
	Exclude []string
}

type MyServer struct {}

const (
    INIT_ARRAY_BUFFER = 8
    SERVER_ADDR = "0.0.0.0:9999"
	INIT_DB_NAME = "dictionary_data.db"
	WEB_SRC_FOLDER = "./websrc"

	QUERY_COUNT_ENTRY = "SELECT COUNT(*) AS count FROM entry"
	QUERY_TABLE_ENTRY = "SELECT * FROM entry WHERE keyword = ?"
	QUERY_LIST_ALL_GROUP = "SELECT DISTINCT group_name FROM entry_group"
	QUERY_TABLE_DEFINITION = "SELECT definition FROM definition WHERE entry_id = ?"
	QUERY_TABLE_USAGE = "SELECT usage FROM usage WHERE entry_id = ?"
	QUERY_TABLE_ENTRY_GROUP = "SELECT group_name FROM entry_group WHERE entry_id = ?"

	QUERY_UPDATE_ENTRY = "UPDATE entry SET pronounciation = ? WHERE id = ?"

	QUERY_DELETE_ALL_DEFINITION = "DELETE FROM definition WHERE entry_id = ?"
	QUERY_DELETE_ALL_USAGE = "DELETE FROM usage WHERE entry_id = ?"
	QUERY_DELETE_ALL_GROUP = "DELETE FROM entry_group WHERE entry_id = ?"

	QUERY_INSERT_ENTRY = "INSERT INTO entry(keyword, pronounciation, last_read) VALUES(?, ?, ?)"
	QUERY_INSERT_DEFINITION = "INSERT INTO definition(entry_id, definition) VALUES(?, ?)"
	QUERY_INSERT_USAGE = "INSERT INTO usage(entry_id, usage) VALUES(?, ?)"
	QUERY_INSERT_GROUP = "INSERT INTO entry_group(entry_id, group_name) VALUES(?, ?)"
)

var (
	db *sql.DB
	data_mutex sync.Mutex
	web_root string
    server_data_file_path string
    content_types = map[string]string{
        ".html": "text/html",
        ".css": "text/css",
        ".js": "text/javascript",
    }
	current_learn_filter = Filter{
		Include: make([]string, 0, INIT_ARRAY_BUFFER),
		Exclude: make([]string, 0, INIT_ARRAY_BUFFER),
	}
)

func dump_request(req *http.Request) {
    req_str, _ := httputil.DumpRequest(req, true)
    fmt.Println(string(req_str))
}

func process_suggest(wt http.ResponseWriter, req *http.Request) {
//     key := req.URL.Query().Get("key")
//     // log(INFO, "Client request for suggestion of `%s`",  key)
//     result := make([]string, 0, INIT_ARRAY_BUFFER)
//     for word, _ := range Dict {
//         if strings.Contains(word, key) {
//             result = append(result, word)
//         }
//     }
//     json_data, err := json.Marshal(struct{ Suggestion []string }{ result })
//     if check_err(err, false, "Can't parse json for `suggest` request") {
//         wt.WriteHeader(http.StatusInternalServerError)
//         wt.Write([]byte(log_format(ERROR, err.Error())))
//     } else {
//         wt.Header().Set("Content-Type", "application/json")
//         wt.WriteHeader(http.StatusOK)
//         wt.Write(json_data)
//     }
}

func query_definitions(entry_id int64) []string {
	rows, err := db.Query(QUERY_TABLE_DEFINITION, entry_id)
	check_err(err, true, "Could not make query for definition with id")
	defer rows.Close()
	result := make([]string, 0)
	for rows.Next() {
		var definition string
		err = rows.Scan(&definition)
		check_err(err, true, "Could not scan for definition in row")
		result = append(result, definition)
	}
	return result
}

func query_usages(entry_id int64) []string {
	rows, err := db.Query(QUERY_TABLE_USAGE, entry_id)
	check_err(err, true, "Could not make query for usage with id")
	defer rows.Close()
	result := make([]string, 0)
	for rows.Next() {
		var usage string
		err = rows.Scan(&usage)
		check_err(err, true, "Could not scan for usage in row")
		result = append(result, usage)
	}
	return result
}

func query_groups(entry_id int64) []string {
	rows, err := db.Query(QUERY_TABLE_ENTRY_GROUP, entry_id)
	check_err(err, true, "Could not make query for group with id")
	defer rows.Close()
	result := make([]string, 0)
	for rows.Next() {
		var group_name string
		err = rows.Scan(&group_name)
		check_err(err, true, "Could not scan for group in row")
		result = append(result, group_name)
	}
	return result
}

func process_query(wt http.ResponseWriter, req *http.Request) {
	key := req.URL.Query().Get("key")
	rows, err := db.Query(QUERY_TABLE_ENTRY, key)
	check_err(err, true, "Could not make query for entry with key " + key)
	defer rows.Close()
	if rows.Next() {
		var id int64
		var keyword string
		var pronounciation string 
		// TODO: change last_read into last_learned
		var last_read int64
		err = rows.Scan(&id, &keyword, &pronounciation, &last_read)
		check_err(err, true, "Could not scan for data in entry table")
		definitions := query_definitions(id)
		usages := query_usages(id)
		groups := query_groups(id)
		entry := Entry {
			Id: id,
			Keyword: keyword,
			Pronounciation: pronounciation,
			Definition: definitions,
			Usage: usages,
			Group: groups,
			LastLearned: last_read,
		}
		json_data, err := json.Marshal(entry)
		check_err(err, true, "Can't marshal entry into json")
		wt.Header().Set("Content-Type", "application/json")
		wt.WriteHeader(http.StatusOK)
		wt.Write(json_data)
	} else {
		wt.WriteHeader(http.StatusNotFound)
		wt.Write([]byte(log_format(WARNING, "No entry for %s", key)))
	}
}

func process_update(wt http.ResponseWriter, req *http.Request, entry Entry) {
	tx, err := db.Begin()
	check_err(err, true, "Could not start transaction")

	rows, err := tx.Query(QUERY_TABLE_ENTRY, entry.Keyword)
	check_err(err, true, "Could not make query for entry with key " + entry.Keyword)

	id := entry.Id
	if rows.Next() { // has entry, then we are trying to upadte
		var keyword string
		var pronounciation string 
		// TODO: change last_read into last_learned
		var last_read int64
		err = rows.Scan(&id, &keyword, &pronounciation, &last_read)
		check_err(err, true, "Could not scan for data in entry table")

		log(INFO, "Entry id = %d keyword = %s existed, updating...", id, entry.Keyword)

		_, err = tx.Exec(QUERY_UPDATE_ENTRY, entry.Pronounciation, id)
		check_err(err, true, fmt.Sprintf("Could not update entry with id = %d", id))

		_, err = tx.Exec(QUERY_DELETE_ALL_DEFINITION, id)
		check_err(err, true, fmt.Sprintf("Could not delete old definitions for entry with id = %d", id))

		_, err = tx.Exec(QUERY_DELETE_ALL_USAGE, id)
		check_err(err, true, fmt.Sprintf("Could not delete old usages for entry with id = %d", id))

		_, err = tx.Exec(QUERY_DELETE_ALL_GROUP, id)
		check_err(err, true, fmt.Sprintf("Could not delete old groups for entry with id = %d", id))
	} else { 		 // add new

		log(INFO, "Entry keyword = %s does not exist, add new...", entry.Keyword)

		_, err = tx.Exec(QUERY_INSERT_ENTRY, entry.Keyword, entry.Pronounciation, time.Now().Unix())
		check_err(err, true, fmt.Sprintf("Could not insert new entry for entry with keyword = %s, pronounciation = %s", entry.Keyword, entry.Pronounciation))
	}

	for _, definition := range entry.Definition {
		_, err = tx.Exec(QUERY_INSERT_DEFINITION, id, definition)
		check_err(err, true, fmt.Sprintf("Could not insert new definition for entry with id = %d, definition = %s", id, definition))
	}

	for _, usage := range entry.Usage {
		_, err = tx.Exec(QUERY_INSERT_USAGE, id, usage)
		check_err(err, true, fmt.Sprintf("Could not insert new usage for entry with id = %d, usage = %s", id, usage))
	}

	for _, group := range entry.Group {
		_, err = tx.Exec(QUERY_INSERT_GROUP, id, group)
		check_err(err, true, fmt.Sprintf("Could not insert new group for entry with id = %d, group = %s", id, group))
	}

	err = tx.Commit()
	check_err(err, true, "Could not commit transaction when update entry")
}

func is_fit_filter(entry Entry, filter Filter) bool {
	included_pass := !slices.ContainsFunc(filter.Include, func(group string) bool {
		return !slices.Contains(entry.Group, group)
	})
	excluded_pass := !slices.ContainsFunc(entry.Group, func(group string) bool {
		return slices.Contains(filter.Exclude, group)
	})
	return included_pass && excluded_pass
}

func list_words_filtered(filter Filter, list *[]string) {
	panic("Unimplemented")
// 	for _, entry := range Dict {
// 		if is_fit_filter(entry, filter) {
// 			*list = append(*list, entry.Keyword)
// 		}
// 	}
// 	// NOTE: sort so that recently used words position at start
// 	// 	     because pop out from the back is much easier and it does not change order of list
// 	sort.Slice(*list, func(i, j int) bool {
// 		return Dict[(*list)[i]].LastLearned > Dict[(*list)[j]].LastLearned
// 	})
}

func process_list(wt http.ResponseWriter, req *http.Request) {
	panic("Unimplemented")
//	query := req.URL.Query()
//
//    var list []string
//	if query.Has("filter") {
//		list = make([]string, 0, INIT_ARRAY_BUFFER)
//		var filter Filter
//		json.Unmarshal([]byte(query.Get("filter")), &filter)
//		list_words_filtered(filter, &list)
//	} else {
//		list = make([]string, 0, len(Dict))
//		for keyword, _ := range Dict { list = append(list, keyword) }
//	}
//    json_data, err := json.Marshal(list)
//    if check_err(err, false, "Can't parse json for `list` request") {
//        wt.WriteHeader(http.StatusInternalServerError)
//        wt.Write([]byte(log_format(ERROR, err.Error())))
//    } else {
//        log(INFO, "Sent %d words", len(list))
//        wt.Header().Set("Content-Type", "application/json")
//        wt.WriteHeader(http.StatusOK)
//        wt.Write(json_data)
//    }
}

func process_serve_file(wt http.ResponseWriter, req *http.Request) {
    file_path := web_root
    if req.URL.Path == "/" {
        file_path += "/index.html"
    } else {
        file_path += req.URL.Path
    }
    file_path = filepath.FromSlash(file_path)

    data, err := os.ReadFile(file_path)
    if check_err(err, false, "Can't read file " + file_path) {
        wt.WriteHeader(http.StatusNotFound)
		wt.Write([]byte(log_format(ERROR, "Can't serve file %s, %s", file_path, err.Error())))
    } else {
        wt.Header().Set("Content-Type", content_types[filepath.Ext(file_path)])
        wt.Write(data)
    }
}

func process_list_group(wt http.ResponseWriter, req *http.Request) {
	rows, err := db.Query(QUERY_LIST_ALL_GROUP)
	check_err(err, true, "Could not make list all group query")
	groups := make([]string, 0, 20)
	for rows.Next() {
		var group_name string
		err = rows.Scan(&group_name)
		check_err(err, true, "Could not scan for group_name in row")
		groups = append(groups, group_name)
	}
	log(INFO, "Sent %d groups", len(groups))
	json_data, err := json.Marshal(struct{ Group []string }{ groups })
	check_err(err, true, "Can't marshal groups into json")
	wt.Header().Set("Content-Type", "application/json")
	wt.WriteHeader(http.StatusOK)
	wt.Write(json_data)
}

func process_nextword(wt http.ResponseWriter, req *http.Request) {
	panic("Unimplemented")
//     if len(unused_words) == 0 { // switch used and unused
//         if len(used_words) == 0 {
//             wt.WriteHeader(http.StatusOK)
//             fmt.Fprint(wt, "No words to learn")
//             return
//         }
// 		list_words_filtered(current_learn_filter, &unused_words)
// 		for i := len(used_words)-1; i >= 0; i-- {
// 			if is_fit_filter(Dict[used_words[i]], current_learn_filter) {
// 				used_words[i] = used_words[len(used_words)-1]
// 				used_words = used_words[:len(used_words)-1]
// 			}
// 		}
//         log(INFO, "Switch used and unused")
//         log(INFO, "%d words left to learn.", len(unused_words))
//     }
// 	if len(unused_words) == 0 {
// 		wt.WriteHeader(http.StatusOK)
// 		fmt.Fprint(wt, "No words to learn")
// 		return
// 	}
//     index := len(unused_words)-1
//     key := unused_words[index]
//     if entry, found := Dict[key]; found {
//         json_data, err := json.Marshal(entry)
//         if check_err(err, false, "Can't parse json for `nextword` request") {
//             wt.WriteHeader(http.StatusInternalServerError)
//             wt.Write([]byte(log_format(ERROR, err.Error())))
//         } else {
//             wt.Header().Set("Content-Type", "application/json")
//             wt.WriteHeader(http.StatusOK)
//             wt.Write(json_data)
// 			// TODO: Maybe some data race will happen here because i also change unused_words and used_words when add new entry
// 			//       which probably run in different thread
// 			// TODO: Maybe add a mutex
// 			unused_words[index] = unused_words[len(unused_words)-1]
// 			unused_words[len(unused_words)-1] = key
// 			unused_words = unused_words[:len(unused_words)-1]
// 			used_words = append(used_words, key)
// 			save_used_words()
// 
// 			// update last check out this key
// 			last_used := entry.LastLearned
// 			entry.LastLearned = time.Now().Unix()
// 			Dict[key] = entry
// 			save_dict()
// 
// 			log(INFO, "Sent entry `%s`, last used: `%s`", entry.Keyword, time.Unix(last_used, 0))
//         }
//     } else {
//         log(ERROR, "Can't find",  key, len(unused_words), len(Dict))
//         wt.WriteHeader(http.StatusInternalServerError)
//         fmt.Fprintf(wt, "[ERROR] Unused list probably is invalid '%s'", key)
//     }
}

func remove_entry(word string) {
// 	for _, group := range Dict[word].Group {
// 		if len(Group[group]) == 1 {
// 			if Group[group][0] != word {
// 				panic("-_-")
// 			}
// 			delete(Group, group)
// 		}
// 	}
// 	delete(Dict, word)
// 	if idx := slices.Index(unused_words, word); idx != -1 {
// 		unused_words[idx] = unused_words[len(unused_words)-1]
// 		unused_words = unused_words[:len(unused_words)-1]
// 	} else if idx := slices.Index(used_words, word); idx != -1 {
// 		used_words[idx] = used_words[len(used_words)-1]
// 		used_words = used_words[:len(used_words)-1]
// 	}
}

func (sv MyServer) ServeHTTP(wt http.ResponseWriter, req *http.Request) {
    wt.Header().Set("Access-Control-Allow-Origin", req.Header.Get("Origin"))
	data_mutex.Lock()
	defer data_mutex.Unlock()
    switch req.Method {
    case "GET":
		log(INFO, "Client request for '%s' with query: %s",  req.URL.EscapedPath(), req.URL.Query())
        if req.URL.Path == "/query" {
            process_query(wt, req)
        } else if (req.URL.Path == "/suggest") {
            process_suggest(wt, req)
        } else if (req.URL.Path == "/list") {
            process_list(wt, req)
        } else if (req.URL.Path == "/nextword") {
            process_nextword(wt, req)
        } else if (req.URL.Path == "/change-learn-filter") {
			panic("Unimplemented")
        } else if (req.URL.Path == "/list-group") {
			process_list_group(wt, req)
        } else {
            process_serve_file(wt, req)
        }
    case "POST":
        var entry Entry
        err := json.NewDecoder(req.Body).Decode(&entry)
		log(INFO, "Client request for '%s' with body: %s",  req.URL.EscapedPath(), entry.str())
        if check_err(err, false, "Can't read POST request body") {
            dump_request(req)
            wt.WriteHeader(http.StatusInternalServerError)
			wt.Write([]byte(log_format(ERROR, "Can't read body, %s", err.Error())))
        } else {
			process_update(wt, req, entry)
        }
    case "DELETE":
        var words []string
        err := json.NewDecoder(req.Body).Decode(&words)
        if check_err(err, false, "Can't read DELETE request body") {
            dump_request(req)
            wt.WriteHeader(http.StatusInternalServerError)
            wt.Write([]byte(log_format(ERROR, "Can't read body, %s", err.Error())))
        } else {
        }
    case "OPTIONS":
        wt.Header().Set("Access-Control-Allow-Methods", "OPTIONS, GET, POST, DELETE")
        wt.Header().Set("Access-Control-Allow-Headers", "Content-Length, Content-Type")
    default:
        dump_request(req)
    }
}

func start_default_browser() {
    var attr os.ProcAttr
    switch runtime.GOOS {
    case "windows":
        _, err := os.StartProcess("C:\\Windows\\System32\\cmd.exe", []string{"C:\\Windows\\System32\\cmd.exe", "http://" + SERVER_ADDR}, &attr)
        check_err(err, false, "Can't start default server", fmt.Sprintf("Please open `http://%s` on a browser\n", SERVER_ADDR))
    case "linux":
        _, err := os.StartProcess("/usr/bin/xdg-open", []string{"/usr/bin/xdg-open", "http://" + SERVER_ADDR}, &attr)
        check_err(err, false, "Can't start default server", fmt.Sprintf("Please open `http://%s` on a browser\n", SERVER_ADDR))
    default:
        log(WARNING, "Unknown platform, the program may not work correctly")
        log(INFO, "Please open `http://%s` on a browser",  SERVER_ADDR)
    }
}

func count_entry() int64 {
	rows, err := db.Query(QUERY_COUNT_ENTRY)
	check_err(err, true, "Could not count number of entries")
	defer rows.Close()
	if rows.Next() {
		var count int64
		err = rows.Scan(&count)
		check_err(err, true, "Could not scan for count column")
		return count
	} else {
		return 0
	}
}

func main() {
	exec_path, err := os.Executable()
	if err != nil {
		log(ERROR, "Could not determine executable path, error: %s", err)
		os.Exit(1)
	}

	exec_dir := filepath.Dir(exec_path)
    if (len(os.Args) == 2) {
        server_data_file_path = os.Args[1]
    } else {
        server_data_file_path = fmt.Sprintf("%s/%s", exec_dir, INIT_DB_NAME)
	}

	if _, err := os.Stat(server_data_file_path); errors.Is(err, os.ErrNotExist) {
		// TODO: create new database
		panic("Unimplemented")
	}
	db, err = sql.Open("sqlite3", server_data_file_path)
	check_err(err, true, "Could not open database " + server_data_file_path)
	defer db.Close()

	web_root = fmt.Sprintf("%s/%s", exec_dir, WEB_SRC_FOLDER)

    log(INFO, "Web root in %s",  web_root)
    log(INFO, "Dictionary file `%s`",  server_data_file_path)
	log(INFO, "Number of entries: %d", count_entry())
    start_default_browser()
	log(INFO, "Server start on http://%s",  SERVER_ADDR)
    http.ListenAndServe(SERVER_ADDR, MyServer{})
}

type LogLevel int
const (
	WARNING LogLevel = iota
	ERROR LogLevel = iota
	INFO LogLevel = iota
)

func log_format(level LogLevel, format string, args ...any) string {
	var builder strings.Builder
	switch level {
	case WARNING:
		builder.WriteString("[Warning] ")
	case ERROR:
		builder.WriteString("[Error]   ")
	case INFO:
		builder.WriteString("[Info]    ")
	}
	builder.WriteString(fmt.Sprintf(format, args...))
	builder.WriteByte('\n')
	return builder.String()
}

func log(level LogLevel, format string, args ...any) {
	fmt.Print(log_format(level, format, args...))
}

func check_err(err error, fatal bool, info ...string) bool {
    if err != nil {
		var log_level LogLevel
        if fatal {
			log_level = ERROR
		} else {
			log_level = WARNING
		}
		log(log_level, err.Error())
        for _, v := range info {
			log(ERROR, "\t %s", v)
        }
        if fatal {
            os.Exit(1)
        }
        return true
    }
    return false
}
