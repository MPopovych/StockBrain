# StockBrain
Lightweight Java-Kotlin based library for neural networks
I am using this for my personal project for stock trading. Comes with risks and frequent changes.
Feel free to fork or to explore the project

#### Why in Kotlin + Java? 
To focus on a CPU based use case for reinforcement learning.
Two algorithms are supported for optimisation:
Genetic algorithm - mostly done and stable
PSO - in development

### Supported layers
*InputLayer* - defines entry for a set of data, multiple inputs can be used in the same model

*Dense* - the most basic layer, can have an activation function, bias

*Activation* - wrapper around the activation function

*Flatten* - turns an {X,Y} layer into a {X*Y, 1} array

*Concat* - turns a list of layers into one (layers have to be of same height)

*ConvDelta* - turn a layer of {X,Y} into {X,Y-1}, applies subtraction between two rows

*FeatureConv* - applies a convolution feature wise (by {X} indicies)
Uses a sliding window of *kernelSize* to produce rows of height *units* and same width as input

*FeatureDense* - applies a convolution feature wise (by {X} indicies)
Uses a fully connected weight to produce rows of height *units* and same width as input

... and others

This a set of components developed for genetic algorithms coupled with a deep neural network.
As a reference for declaration style tensorflow-keras was used

``` kotlin
val input = InputLayer(3)

val d0 = Dense(4, Activations.ReLu, name = "d0") { input }

val d1 = Dense(4, Activations.ReLu, name = "d1") { d0 }
val d2 = Dense(4, Activations.ReLu, name = "d2") { d0 }

val concat = Concat { listOf(d1, d2) }

val builder = ModelBuilder(input, concat)
builder.build(debug = false)
```

Model summary of above:

``` 
Total layers: 5
InputLayer_0 : LayerShape(width=3, height=1) : children: 1
d0 : LayerShape(width=4, height=1) : children: 2
d1 : LayerShape(width=4, height=1) : children: 1
d2 : LayerShape(width=4, height=1) : children: 1
Concat_4 : LayerShape(width=8, height=1) : children: 0
``` 

### Inputs and output

Models support multi-input and multi-output
``` kotlin
val inputLayer1 = InputLayer(3, 2, name = "input1")
val inputLayer2 = InputLayer(6, 1, name = "input2")
val d0_t = Dense(4, activation = Activations.ReLu, useBias = false) { inputLayer1 }
val conv_d = ConvDelta { d0_t }
val d1 = Direct(activation = Activations.ReLu) { inputLayer2 }
val outputLayer = Concat { listOf(conv_d, d1) }
val model = ModelBuilder(mapOf("1" to inputLayer1, "2" to inputLayer2), outputLayer).build()

val array1 = floatArrayOf(0.4f, 0f, 1f, 4f, 0f, 0f).reshapeToMatrix(3, 2)
val array2 = floatArrayOf(1.4f, 2f, 0.5f, 0f, 1f, -2f).reshapeToMatrix(6, 1)
val outputMatrix = model.getOutput(mapOf("1" to array1, "2" to array2))
outputMatrix.printRed()
// [2.577, 0, 1.322, 0, 0.97, 1.3, 0.154, 0, 0.975, 1.171]
```

Or singular input:
``` kotlin
val inputLayer = InputLayer(3, 2)
val d0 = Dense(4) { inputLayer }
val convDelta = ConvDelta { d0 }
val model = ModelBuilder(inputLayer, convDelta).build()

val array1 = floatArrayOf(0.4f, 0f, 1f, 4f, 0f, 0f).reshapeToMatrix(3, 2)
val outputMatrix = model.getOutput(array1)
outputMatrix.printRed()
// [-0.134, -2.884, 2.682, 1.099]
```

### Saving and loading the model

Model is serialized into a json

``` kotlin
val input = InputLayer(3)
val d0 = Dense(4, activation = Activations.ReLu, name = "d0", useBias = false) { input }
val d1 = Direct(activation = Activations.ReLu, name = "d2") { input }
val model = ModelBuilder(input, Concat(name = "output") { listOf(d0, d1) }).build()

val sm = ModelWriter.serialize(model)
val json = ModelWriter.toJson(sm)
``` 
Which would produce a structure as below

``` json
{
  "inputs": {
    "Default": "Input_0"
  },
  "outputs": {
    "Default": "output"
  },
  "layers": [
    {
      "name": "Input_0",
      "nameType": "Input",
      "width": 3,
      "height": 1
    },
    {
      "name": "d0",
      "nameType": "Dense",
      "width": 4,
      "height": 1,
      "activation": "activation.relufunction",
      "weights": [
        {
          "name": "weight",
          "value": "P02cTL6i7UC/KnmGvoK8yL1z5MA/TLA8vy+uLL8q1NQ+imqIvyfIfL7qnMi/PFxc"
        }
      ],
      "parents": [
        "Input_0"
      ],
      "builderData": {
        "useBias": false
      }
    },
    {
      "name": "d2",
      "nameType": "Direct",
      "width": 3,
      "height": 1,
      "activation": "activation.relufunction",
      "weights": [
        {
          "name": "weight",
          "value": "Px6BYD9gGzS/PNSs"
        },
        {
          "name": "bias",
          "value": "AAAAAAAAAAAAAAAA"
        }
      ],
      "parents": [
        "Input_0"
      ],
      "builderData": {
        "useBias": true
      }
    },
    {
      "name": "output",
      "nameType": "Concat",
      "width": 7,
      "height": 1,
      "parents": [
        "d0",
        "d2"
      ]
    }
  ]
}
``` 

Creating a model from a json should be done like this:
``` kotlin
val model = ModelReader.modelInstance(json)
``` 
Model summary for above:
``` 
Total layers: 4 : inputs: [Default], outputs: [Default]
Input_0 : LayerShape(width=3, height=1) : children: 2
d0 : LayerShape(width=4, height=1) : children: 1
d2 : LayerShape(width=3, height=1) : children: 1
output : LayerShape(width=7, height=1) : children: 0
``` 

## GA

### Instance building
``` kotlin
val settings = GASettings(
    topParentCount = 5,
    totalPopulationCount = 10,
    scoreBoardOrder = GAScoreBoardOrder.Descending,
    initialMutationPolicy = AdditiveMutationPolicy(1.0),
    mutationPolicy = CyclicMutationPolicy(0.3),
)

val ga = GA(settings, model, earlyStopCallback = { i, ga ->
    val top = ga.scoreBoard.getTop()?.score ?: return@GA false
    if (top == 0.0) {
        printRed("Stop on gen $i with $top")
        return@GA true
    }
    return@GA false
})
```

**topParentCount** - limitation to scoreboard, top scores will be used for crossovers and mutation

**totalPopulationCount** - how many of new challengers will be produced

**scoreBoardOrder** - possible values: *GAScoreBoardOrder.Descending* and *GAScoreBoardOrder.Ascending*

**GAScoreBoardOrder.Ascending** - try to achieve the highest score

**GAScoreBoardOrder.Descending** - try to achieve the lowest score

``` kotlin
ga.runFor(generations =100000, silent = true) { context ->
    val outputArray = inputData.map { input -> context.model.getOutput(input) }
    return@runFor getScoreCustomFunction(outputArray)
}

ga.scoreBoard.printScoreBoard()
val top = ga.scoreBoard.getTop() ?: throw IllegalStateException()
top.genes.applyToModel(model)
val outputMatrix = model.getOutput(inputMatrix)
```

# Examples

``` kotlin
fun create_v1_mc4(features: Int): Model {
    val input = InputLayer(features = features, steps = 1)
    val hadamardInput = Direct(useBias = false) { input }
    
    var branchAsk: LB = Dense(12, activation = Activations.Tanh) { hadamardInput }
    branchAsk = Dense(4, activation = Activations.Tanh) { branchAsk }
    branchAsk = Dense(4, activation = Activations.Tanh) { branchAsk }
    
    var branchBid: LB = Dense(12, activation = Activations.Tanh) { hadamardInput }
    branchBid = Dense(8, activation = Activations.Tanh) { branchBid }
    branchBid = Dense(4, activation = Activations.Tanh) { branchBid }
    
    val concat = Concat { listOf(branchAsk, branchBid) }
    val hadamardOutput = Direct { concat }
    val output = Dense(2, activation = Activations.Sigmoid) { hadamardOutput }
    
    return ModelBuilder(input, output).build()
}
```