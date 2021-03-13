package BQN.tokenizer.types;

import BQN.Scope;
import BQN.errors.SyntaxError;
import BQN.tokenizer.Token;
import BQN.types.*;
import BQN.types.arrs.*;

public class NameTok extends Token {
  public final String name;
  public final String rawName;
  public Value val;
  
  public NameTok(String line, int spos, int epos, String rawName, Value[] args) {
    super(line, spos, epos);
    this.rawName = rawName;
    this.type = varType(rawName);
    if (rawName.length()<=2 && type=='d') throw new SyntaxError("\""+rawName+"\" is an invalid name", this);
    boolean isSys = rawName.charAt(0) == '•';
    String name0 = (isSys? rawName.substring(1) : rawName).toLowerCase();
    String name1 = name0.replace("_", "");
    name = isSys? '•'+name1 : name1;
    if (isSys) switch (Scope.rel(name)) {
      case 1: if (args==null||args[0]==null) throw new SyntaxError("•path hasn't been defined", this); val=args[0]; break;
      case 2: if (args==null||args[1]==null) throw new SyntaxError("•name hasn't been defined", this); val=args[1]; break;
      case 3: if (args==null||args[2]==null) throw new SyntaxError("•args hasn't been defined", this); val=args[2]; break;
      case 4:
        if (args==null) val = EmptyArr.SHAPE0SV;
        else val = new HArr(args);
        break;
      case 5:
        if (args==null) val = Nothing.inst;
        else val = args[0];
        break;
    }
  }
  public static char varType(String name) {
    char s = name.charAt(0);
    if (s=='•') s = name.charAt(1);
    if (s=='_') {
      char e = name.charAt(name.length()-1);
      return e=='_'? 'd' : 'm';
    }
    if (s>='A' && s<='Z') return 'f';
    return 'a';
  }
  
  @Override public String toTree(String p) {
    return p+"name: " + rawName + "\n";
  }
  
  @Override public String toRepr() {
    return rawName;
  }
}