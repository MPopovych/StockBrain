# StockBrain
Lightweight Java-Kotlin based library for neural networks

This a set of components developed for genertic algorithms coupled with a deep neural network.
As a reference for declaration style tensorflow-keras was used

``` kotlin

val input = InputLayer(3)

val d0 = Dense(4, Activations.ReLu, name = "d0") { input }

val d1 = Dense(4, Activations.ReLu, name = "d1") { d0 }
val d2 = Dense(4, Activations.ReLu, name = "d2") { d0 }

val concat = Concat { listOf(d1, d2) }

val builder = ModelBuilder(input, concat)
builder.build(debug = false)

printYellow(builder.summary())

```

Summary:

``` 
Total layers: 5
InputLayer_0 : LayerShape(width=3, height=1) : children: 1
d0 : LayerShape(width=4, height=1) : children: 2
d1 : LayerShape(width=4, height=1) : children: 1
d2 : LayerShape(width=4, height=1) : children: 1
Concat_4 : LayerShape(width=8, height=1) : children: 0

``` 
