import 'package:flutter/material.dart';
import 'package:pdict/ListInput.dart';

class EntryData {
  EntryData();
  EntryData.from({required this.key, required this.pronounciation, required this.definitions, required this.usages, required this.groups});
  String key = "Keyword";
  String pronounciation = "testing";
  List<String> definitions = ["Hello world", "How are you today", "I am fine thankyou and you", "Hello world", "How are you today", "I am fine thankyou and you", "Hello world", "How are you today", "I am fine thankyou and you", "Hello world", "How are you today", "I am fine thankyou and you", "Hello world", "How are you today", "I am fine thankyou and you", "Hello world", "How are you today", "I am fine thankyou and you", "Hello world", "How are you today", "I am fine thankyou and you", "Hello world", "How are you today", "I am fine thankyou and you", "Hello world", "How are you today", "I am fine thankyou and you", "Hello world", "How are you today", "I am fine thankyou and you", ];
  List<String> usages = ["Checking out usages", "Nice right"];
  List<String> groups = [];
  int lastLearned = DateTime.now().millisecondsSinceEpoch ~/ 1000;
}

class Entry extends StatefulWidget {
  const Entry({super.key,  required this.data});

  final EntryData data;

  @override
  State<StatefulWidget> createState() => EntryState();
}

class EntryState extends State<Entry> {

  late List<TextEditingController> definitionInputControllers;

  late List<TextEditingController> usageInputControllers;

  final labelStyle = const TextStyle(
    fontSize: 20,
    color: Colors.red,
    fontWeight: FontWeight.bold,
  );

  @override
  Widget build(BuildContext context) {
    definitionInputControllers = List.generate(
      widget.data.definitions.length,
      (int index) => TextEditingController(text: widget.data.definitions[index])
    );
    usageInputControllers = List.generate(
      widget.data.usages.length,
      (int index) => TextEditingController(text: widget.data.usages[index])
    );
    return CustomScrollView(
      slivers: <Widget>[
        SliverList.list(
          children: [
            Text(widget.data.key, textAlign: TextAlign.center, style: Theme.of(context).textTheme.headlineMedium),
            TextField(
              decoration: InputDecoration(
                hintText: "Pronounciation",
                hintStyle: Theme.of(context).textTheme.titleLarge,
                border: InputBorder.none,
              ),
              style: Theme.of(context).textTheme.titleLarge,
              textAlign: TextAlign.center
            ),
            Container(
              child: Text("Definition", style: labelStyle),
              margin: EdgeInsets.only(top: 10)
            ),
            ...listInput(definitionInputControllers),
            Container(
              child: Text("Usage", style: labelStyle),
              margin: EdgeInsets.only(top: 10)
            ),
            ...listInput(usageInputControllers)
          ]
        )
      ]
    );
  }

  List<Widget> listInput(List<TextEditingController> controllers) => List.generate(
    controllers.length,
    (int index) => Container(
      height: 50,
      child: TextField(
        controller: controllers[index],
        style: Theme.of(context).textTheme.bodySmall,
        textAlign: TextAlign.left
      ),
    ),
  );
}
