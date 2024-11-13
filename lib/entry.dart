import 'package:flutter/material.dart';

class EntryData {
  EntryData();
  EntryData.from({required this.key, required this.pronounciation, required this.definitions, required this.usages, required this.groups});
  String key = "io12j3io12j3i oj12io3 jio123j io1j2io 3j1o2i j3o1 2j3oj";
  String pronounciation = "";
  List<String> definitions = ["1231i2j 3oi1 2", "JI!J#IO@!J #IO@!J #OI", "j 12io3j 1io2j3oi12j 3oi", "JWQIEJ", "JQWEIO JQWOE", "QWJEIOJQWOEJ OQ", "1231i2j 3oi1 2", "JI!J#IO@!J #IO@!J #OI", "j 12io3j 1io2j3oi12j 3oi", "JWQIEJ", "JQWEIO JQWOE", "QWJEIOJQWOEJ OQ", "1231i2j 3oi1 2", "JI!J#IO@!J #IO@!J #OI", "j 12io3j 1io2j3oi12j 3oi", "JWQIEJ", "JQWEIO JQWOE", "QWJEIOJQWOEJ OQ", "JI!J#IO@!J #IO@!J #OI", "j 12io3j 1io2j3oi12j 3oi", "JWQIEJ", "JQWEIO JQWOE", "QWJEIOJQWOEJ OQ", "1231i2j 3oi1 2", "JI!J#IO@!J #IO@!J #OI", "j 12io3j 1io2j3oi12j 3oi", "JWQIEJ", "JQWEIO JQWOE", "QWJEIOJQWOEJ OQ", "1231i2j 3oi1 2", "JI!J#IO@!J #IO@!J #OI", "j 12io3j 1io2j3oi12j 3oi", "JWQIEJ", "JQWEIO JQWOE", "QWJEIOJQWOEJ OQ", "JI!J#IO@!J #IO@!J #OI", "j 12io3j 1io2j3oi12j 3oi", "JWQIEJ", "JQWEIO JQWOE", "QWJEIOJQWOEJ OQ", "1231i2j 3oi1 2", "JI!J#IO@!J #IO@!J #OI", "j 12io3j 1io2j3oi12j 3oi", "JWQIEJ", "JQWEIO JQWOE", "QWJEIOJQWOEJ OQ", "1231i2j 3oi1 2", "JI!J#IO@!J #IO@!J #OI", "j 12io3j 1io2j3oi12j 3oi", "JWQIEJ", "JQWEIO JQWOE", "QWJEIOJQWOEJ OQ"   ];
  List<String> usages = [];
  List<String> groups = [];
  int lastLearned = DateTime.now().millisecondsSinceEpoch ~/ 1000;
}

class Entry extends StatefulWidget {
  const Entry({super.key,  required this.data});

  final EntryData data;

  @override
  State<StatefulWidget> createState() => EntryWidget();
}

class EntryWidget extends State<Entry> {
  bool isEditting = false;

  @override
  Widget build(BuildContext context) => Column(
    verticalDirection: VerticalDirection.down,
    children: [
      Text(
        widget.data.key,
        textAlign: TextAlign.center,
        style: const TextStyle(
          color: Colors.white,
          fontWeight: FontWeight.w900,
          fontSize: 25,
        ),
      ),
      TextField(
        style: const TextStyle(color: Colors.white),
        autocorrect: false,
        cursorColor: Colors.white,
        textAlign: TextAlign.center,
        enabled: isEditting,
        onChanged: (value) => widget.data.pronounciation = value,
      ),
      Column(
        children: [
          const Text("Definition", textAlign: TextAlign.left, style: TextStyle(color: Colors.white)),
          Container(
            child: ListView.builder(
              physics: const NeverScrollableScrollPhysics(),
              shrinkWrap: true,
              itemCount: widget.data.definitions.length,
              itemBuilder: (BuildContext context, int index) {
                return Text(widget.data.definitions[index], style: const TextStyle(color: Colors.red));
              },
            )
          ),

        ],
      ),
      TextButton(
        onPressed: () => setState(() => isEditting = !isEditting),
        child: const Text("Switch", style: TextStyle(color: Colors.white))
      ),
    ],
  );
}