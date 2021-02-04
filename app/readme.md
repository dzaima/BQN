A PC & Android app made in [Processing](https://processing.org/)

## Usage
1. build dzaima/BQN for java 8 (by running `./build8` in the base directory)
2. run `./cpJar` in the `app` directory
3. Open `app/` in Processing and run

### Compile for Android:
4. Install the Android mode in Processing (see top right corner → `Java`; requires Processing 3) and switch to it
5. In `PS.pde` change the first line to `/*`
6. Either run in Android Mode with a device connected, or File → Export Signed Package to generate an APK


---

```
swipe on characters to enter ones around it
A - switch to text layout
F - change capitalization of the name
⇧ - shift - caps letters, select with «»; double tap to hold
X - close tab (where applicable - grapher & editor)
K - open virtual keyboard
= - save /evaluate (keeps REPL input)
⏎ - enter/evaluate (clears REPL input)

↶↷ - undo/redo
▲▼ - up/down or move trough REPL history
^C/^V - copy/paste

() / {} / ⟨⟩ allow wrapping around selected substring
double-tap line in the history to edit it


:isz sz     change input box font size
:hsz sz     change REPL history font size
:tsz sz     change top bar size
:g expr     graph the expression (editable in the window)
:clear      clear REPL history
:f  path    edit file at the path
:fx path    edit file at the path, executing on save
:ex path    execute file at the path
:ed fn      edit the function by name in another window (= - save, ⏎ - newline, X - save & close)
```