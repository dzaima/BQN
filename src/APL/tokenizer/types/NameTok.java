package APL.tokenizer.types;

import APL.tokenizer.Token;

public class NameTok extends Token {
  public final String name;
  public final String rawName;
  
  public NameTok(String line, int spos, int epos, String rawName) {
    super(line, spos, epos);
    this.rawName = rawName;
    this.type = varType(rawName);
    name = (type=='a' || type=='f'? rawName : type=='d'? rawName.substring(1,rawName.length()-1) : rawName.substring(1)).toLowerCase();
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