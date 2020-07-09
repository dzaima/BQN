package APL.tokenizer;

import APL.types.Tokenable;

public abstract class Token implements Tokenable {
  public char type; // \0 by default
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
    return p + this.getClass().getCanonicalName()+" "+spos+"-"+epos+"\n";
  }
  public abstract String toRepr();
  
  public String source() {
    return raw.substring(spos, epos);
  }
}