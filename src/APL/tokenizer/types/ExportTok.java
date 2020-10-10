package APL.tokenizer.types;

import APL.tokenizer.Token;

public class ExportTok extends Token {
  
  public ExportTok(String line, int spos, int epos) {
    super(line, spos, epos);
    type = '⇐';
    flags = 2;
  }
  
  public String toRepr() {
    return "⇐";
  }
}