package APL.tokenizer.types;

import APL.Scope;
import APL.errors.SyntaxError;
import APL.tokenizer.Token;
import APL.types.*;

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
    String name1 = type=='a' || type=='f'? name0 : type=='d'? name0.substring(1,name0.length()-1) : name0.substring(1);
    name = isSys? '•'+name1 : name1;
    if (isSys) switch (name) {
      case "•args": if (args==null) throw new SyntaxError("using •args outside of file", this); val=args[0]; break;
      case "•name": if (args==null) throw new SyntaxError("using •name outside of file", this); val=args[1]; break;
      case "•path": if (args==null) throw new SyntaxError("using •path outside of file", this); val=args[2]; break;
      default: if (Scope.isRel(name)) {
        if (args==null) val = Nothing.inst; // throw new SyntaxError("using "+name+" outside of file", this);
        else val = args[2];
        break;
      }
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