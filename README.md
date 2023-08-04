# StockBrain

Lightweight Kotlin based library for neural networks
I am using this for my personal project for stock trading. Comes with risks and frequent changes.
Feel free to fork or to explore the project
Uses Kotlin's **Multik** under the hood

#### Why in Kotlin?

To focus on a CPU based use case for reinforcement learning.
One algorithm are supported for optimisation:

- GAT - is a custom implementation of a GA (genetic algorithm)
- PSO - removed
- GA - removed

### Supported layers

*Input* - defines entry for a set of data, multiple inputs can be used in the same model

*Dense* - the most basic layer, can have an activation function, bias

*Activation* - wrapper around the activation function

*Flatten* - turns an {X,Y} layer into a {X*Y, 1} array

*Concat* - turns a list of layers into one (layers have to be of same height)

... and others

This a set of components developed for genetic algorithms coupled with a deep neural network.
As a reference for declaration style tensorflow-keras was used

``` kotlin
val input = Input(3)
val d0 = Dense(4, Activations.ReLu) { input }
val d1 = Dense(4, Activations.ReLu) { d0 }
val d2 = Dense(4, Activations.ReLu) { d0 }
val concat = Concat(d1, d2)
val builder = ModelBuilder(input, concat)
builder.build()
```

### Inputs and output

Models support multi-input and multi-output
