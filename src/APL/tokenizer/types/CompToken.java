package APL.tokenizer.types;

import APL.tokenizer.Token;

public class CompToken extends Token {
  public CompToken(String raw, int spos, int epos) {
    super(raw, spos, epos);
  }
  
  public String toRepr() {
    return source();
  }
}
