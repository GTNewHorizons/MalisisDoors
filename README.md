# [MalisisDoors](http://www.minecraftforum.net/forums/mapping-and-modding/minecraft-mods/2076338-1-7-2-1-7-10-forge-malisisdoors-1-7-10-1-1-2)

This is a fork of [Ordinastie/MalisisDoors](https://github.com/Ordinastie/MalisisDoors/tree/master). It has been disconnected to avoid accidental upstream pull requests.

## Features:

* Adds animations to doors, trap doors and fence gates.
* Adds new animated sliding doors that comes in wood or iron material.
* Adds sensors which detect players passing under and send a redstone signal to the block they are attached to.
* Adds vanishing blocks. Frames can be crafted and placed in the world, when supplied with redstone current, they
vanish into thin air and make all neighboring vanishing blocks to vanish as well. When the redstone current stop,
the frames go back to being solid blocks. A frame can be activated with normal blocks which are used to "paint"
the frame. Different types of frames implies different vanishing propagation behavior :
  - wood frames propagate to all frames around them
  - iron frames propagate to all frames around painted with the same block
  - gold frames propagate to all frames around painted with the same block and the same metadata (ie red wool would not make a blue wool vanish)
  - diamond frames has their own GUI to configure their behavior. You can choose for each direction if they should propagate and the delay
  - Frames not painted automatically propagate their state.
