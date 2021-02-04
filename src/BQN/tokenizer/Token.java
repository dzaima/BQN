package BQN.tokenizer;

import BQN.types.Tokenable;

public abstract class Token implements Tokenable {
  public char type; // \0 by default
  public byte flags = -1; // 1 - is constant; 2 - has no inner blocks; 4 - is safe for variable renaming
  public final String raw;
  public final int spos; // incl
  public final int epos; // excl
  public Token(String raw, int spos, int epos) {
    this.raw = raw;
    this.spos = spos;
    this.epos = epos;
  }
  
  @Override public Token getToken() {
    return this;
  }
  public String toTree(String p) {
    return p + this.getClass().getSimpleName()+" "+spos+"-"+epos+" "+type+" "+flags+"\n";
  }
  public abstract String toRepr();
  
  public String source() {
    return raw.substring(spos, epos);
  }
  
  
  public static Token COMP = new Token("•COMPiled function", 0, 18) {
    public String toRepr() {
      return "•COMPiled function";
    }
  };
}