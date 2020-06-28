package APL.tokenizer.types;

import APL.errors.SyntaxError;
import APL.tokenizer.Token;

public class NameTok extends Token {
  public final String name;
  public final String rawName;
  
  public NameTok(String line, int spos, int epos, String rawName) {
    super(line, spos, epos);
    this.rawName = rawName;
    this.type = varType(rawName);
    if (rawName.length()<=2 && type=='d') throw new SyntaxError("\""+rawName+"\" is an invalid name", this);
    String name0 = (rawName.charAt(0)=='•'? rawName.substring(1) : rawName).toLowerCase();
    String name1 = type=='a' || type=='f'? name0 : type=='d'? name0.substring(1,name0.length()-1) : name0.substring(1);
    this.name = rawName.charAt(0)=='•'? '•'+name1 : name1;
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