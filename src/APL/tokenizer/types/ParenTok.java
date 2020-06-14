package APL.tokenizer.types;

import APL.tokenizer.Token;

public class ParenTok extends Token {
  public final LineTok ln;
  public final boolean hasDmd;
  
  public ParenTok(String line, int spos, int epos, LineTok ln, boolean hasDmd) {
    super(line, spos); end(epos);
    this.ln = ln;
    this.hasDmd = hasDmd;
  }
  
  @Override public String toRepr() {
    return "(" + ln.toRepr() + ")";
  }
}