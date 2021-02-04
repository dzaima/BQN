package BQN.tokenizer.types;

import BQN.tokenizer.Token;

public class CommentTok extends Token {
  public CommentTok(String raw, int spos, int epos) {
    super(raw, spos, epos);
  }
  
  @Override public String toRepr() {
    return source();
  }
}