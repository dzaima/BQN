A PC & Android app for BQN made in [Processing](https://processing.org/); [download APK](https://github.com/dzaima/BQN/releases/latest)

Contains a REPL, a calculator-like keyboard for Android, and a very basic editor. Interprets dzaima/BQN.

### Run on PC:
1. build dzaima/BQN with Java 8 (by running `./build` in the base directory; `./build8` with a newer Java might not work (does not with Java 21))
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
F - change capitalization of the name under the cursor
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
