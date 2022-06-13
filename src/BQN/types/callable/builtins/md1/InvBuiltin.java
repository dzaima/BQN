package BQN.types.callable.builtins.md1;

import BQN.errors.NYIError;
import BQN.tools.FmtInfo;
import BQN.types.*;
import BQN.types.callable.Md1Derv;
import BQN.types.callable.builtins.Md1Builtin;

public class InvBuiltin extends Md1Builtin {
  public String ln(FmtInfo f) { return "⁼"; }
  
  public Value call(Value f, Value x, Md1Derv derv) {
    return f.callInv(x);
  }
  public Value call(Value f, Value w, Value x, Md1Derv derv) {
    return f.callInvX(w, x);
  }
  
  public Value callInv(Value f, Value x) {
    return f.call(x);
  }
  public Value callInvX(Value f, Value w, Value x) {
    return f.call(w, x);
  }
  public Value callInvW(Value f, Value w, Value x) {
    throw new NYIError("⁼ inverting 𝕨", this);
  }
  
  
  
  
  public static Fun invertM(Value f) {
    return new /*IS*/Fun(/*AA Value f*/) {
      public String ln(FmtInfo f) { return "⁼"; }
      public Value call(Value x) {
        return f.callInv(x);
      }
    };
  }
  
  public static Fun invertX(Value f) {
    return new /*IS*/Fun(/*AA Value f*/) {
      public String ln(FmtInfo f) { return "⁼"; }
      public Value call(Value w, Value x) {
        return f.callInvX(w, x);
      }
      
      public Value callInvX(Value w, Value x) {
        return f.call(w, x);
      }
    };
  }
  
  public static Fun invertW(Value f) {
    return new /*IS*/Fun(/*AA Value f*/) {
      public String ln(FmtInfo f) { return "˜⁼˜"; }
      public Value call(Value w, Value x) {
        return f.callInvW(w, x);
      }
      
      public Value callInvW(Value w, Value x) {
        return f.call(w, x);
      }
    };
  }
}