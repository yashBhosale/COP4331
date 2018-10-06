import 'package:flutter/material.dart';
import 'package:flutter/animation.dart';

class TestScreen extends StatefulWidget {
  @override
  _TestScreenState createState() => _TestScreenState();
}

class _TestScreenState extends State<TestScreen> with TickerProviderStateMixin {
  AnimationController _controller;
  Animation<Offset> animation;
  Animation<double> disappear;
  bool _visible = true;
  initState() {
    super.initState();
    _controller = AnimationController(
        duration: const Duration(milliseconds: 2000), vsync: this);
    animation = Tween(begin: new Offset(5.0, 0.0), end: new Offset(-3.0, 0.0))
        .animate(_controller);

    _controller.forward();
  }

  @override
  Widget build(BuildContext context) {
    final title = 'test';

    return MaterialApp(
        title: title,
        home: Scaffold(
            appBar: AppBar(
              title: Text(title),
            ),
            body: new Builder(builder: (BuildContext context) {
              return new Center(
                  child: SlideTransition(
                      position: animation,
                      child: AnimatedOpacity(
                        opacity: _visible ? 1.0 : 0.0,
                        duration: Duration(milliseconds: 200),
                        child: IconButton(
                            icon: new Icon(Icons.add_circle),
                            color: Colors.red,
                            iconSize: 100.0,
                            onPressed: () {
                              setState(() {
                                //_visible = !_visible;
                                _controller.reset();
                                _controller.forward();
                              });
                            }),
                      )));
            })));
  }
}
