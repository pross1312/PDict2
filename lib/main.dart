import 'package:flutter/material.dart';
import 'package:pdict/ListInput.dart';
import 'package:pdict/entry.dart';

void main() => runApp(const PDict());

class PDict extends StatelessWidget {
  const PDict({super.key});

  ThemeData _gruberDarkerTheme() {
    return ThemeData(
      brightness: Brightness.dark,
      primaryColor: const Color(0xFF181818),
      scaffoldBackgroundColor: const Color(0xFF181818),
      cardColor: const Color(0xFF282828),
      appBarTheme: const AppBarTheme(
        backgroundColor: Color(0xFF181818),
        centerTitle: true,
        titleTextStyle: TextStyle(
          color: Color(0xFFD0D0D0),
          fontSize: 35,
          fontWeight: FontWeight.bold,
        ),
        iconTheme: IconThemeData(color: Color(0xFFD0D0D0)),
      ),
      textTheme: const TextTheme(
        bodySmall: TextStyle(color: Color(0xFFD0D0D0), fontSize: 18),
        bodyMedium: TextStyle(color: Color(0xFFD0D0D0), fontSize: 20),
        headlineMedium: TextStyle(color: Color(0xFFD0D0D0), fontSize: 30, fontWeight: FontWeight.bold),
        titleLarge: TextStyle(color: Color(0xFFB0B0B0)),
      ),
      buttonTheme: const ButtonThemeData(
        buttonColor: Color(0xFFA03636),
        textTheme: ButtonTextTheme.primary,
      ),
      iconTheme: const IconThemeData(color: Color(0xFFD0D0D0)),
      colorScheme: const ColorScheme.dark(
        primary: Color(0xFFA03636),
        secondary: Color(0xFF668799),
        surface: Color(0xFF282828),
        background: Color(0xFF181818),
        onBackground: Color(0xFFD0D0D0),
        onPrimary: Color(0xFFFFFFFF),
        onSecondary: Color(0xFFD0D0D0),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      theme: _gruberDarkerTheme(),
      title: 'PDict',
      home: const HomePage(),
    );
  }
}

class HomePage extends StatefulWidget {
  const HomePage({super.key});

  @override
  State<HomePage> createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> {

  EntryData entryData = EntryData();

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xff303030),
      body: Container(
        padding: EdgeInsets.all(10.0),
        child: Column(
          children: [
            IntrinsicHeight(
              child: Row(
                crossAxisAlignment: CrossAxisAlignment.stretch,
                children: <Widget>[
                  Container(
                    alignment: Alignment.center,
                    color: Theme.of(context).appBarTheme.backgroundColor,
                    child: Text("PDict", style: Theme.of(context).appBarTheme.titleTextStyle),
                    margin: EdgeInsets.only(right: 20),
                  ),
                  Expanded(child: searchBox())
                ]
              )
            ),
            Expanded(flex: 1, child: Container(
              padding: const EdgeInsets.only(top: 10),
              child: Entry(data: entryData),
            ))
          ]
        )
      )
    );
  }

  void query(String keyword) {
    setState(() {
      entryData.key = keyword;
    });
  }

  Widget searchBox() {
    return TextField(
      decoration: InputDecoration(
        hintText: "Keyword",
        border: OutlineInputBorder(),
        hintStyle: Theme.of(context).textTheme.bodySmall
      ),
      style: Theme.of(context).textTheme.bodyMedium,
      onSubmitted: query,
    );
  }
}
