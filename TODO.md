# StockBrain todo

Added:

- investigate nd4f, EJML compose matrix multiplication (07.03.2023)

Done:

- add working directory helper for saving json files (12.11.2022)
- add score board reset option on every generation + retesting of top 3 (verified 07.03.2023)
- added FeatureConv and GRU layers (07.03.2023)
- added some experimental layers
- added utilities like ModelFrame and ScaleFilter to convert object into input arrays (07.03.2023)
- refactored the implementation to ROW-MAJOR algorithm

Delayed:

Rejected (provide commentary):

- add coroutine support for GA action paralleling (12.11.2022)
  better be used outside to simplify the library
- add InputLayer wrapper to filter out timesteps or features (07.03.2023)
  use multi-branch structure as separate inputs by manually building the input arrays
