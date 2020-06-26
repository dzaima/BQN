package APL.tokenizer.types;

import APL.errors.SyntaxError;
import APL.tokenizer.Token;
import APL.types.*;

public class ChrTok extends Token {
  public final Value val;
  
  public ChrTok(String line, int spos, int epos, String str) {
    super(line, spos, epos);
  
    if (str.length() != 1) throw new SyntaxError("single-quote chars must be length 1", this);
    val = Char.of(str.charAt(0)); // +TODO unicode codepoint stuff
    val.token = this;
    type = 'a';
  }
  
  @Override public String toRepr() {
    return source();
  }
}