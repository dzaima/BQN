A [BQN](https://github.com/mlochbaum/BQN) implementation based on dzaima/APL

`./build` to build a `.jar` file

`./REPL` to start a simple REPL

Adding the following to `~/bin/dbqn` will make hashbangs like `#!/usr/bin/env dbqn` execute dzaima/BQN:
```bash
#!/bin/bash

java -jar /path/to/dzaima/BQN/BQN.jar -f "$@"
```

With GraalVM's Native Image, you can build a complete binary (≈10MB, doesn't need Java at all, startup time <10ms):

```bash
native-image --report-unsupported-elements-at-runtime -jar BQN.jar nBQN
```
This generates a regular executable file `nBQN`, usable in place of `java -jar BQN.jar`. Note that this also disables compilation to Java bytecode, and has different performance characteristics to a regular JVM.


### [app](https://github.com/dzaima/BQN/tree/master/app)
A BQN-specific app with syntax highlighting and most regular text editor stuff (very much work in progress)

Works on Linux and Android (should work on Windows, assuming bash is available to compile BQN itself)