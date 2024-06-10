package BQN.types.callable.builtins;

import BQN.errors.*;
import BQN.tools.FmtInfo;
import BQN.types.*;
import BQN.types.arrs.*;
import BQN.types.mut.SimpleMap;

import java.util.*;
import java.nio.file.*;

public class FileNS extends SimpleMap {
  public String ln(FmtInfo f) { return "(file)"; }
  
  private final Path path;
  public FileNS(Path path) {
    this.path = path;
  }
  
  private final Value at = new FB("At") {
    public Value call(Value x) {
      return new ChrArr(path.resolve(x.asString()).toString());
    }
    public Value call(Value w, Value x) {
      return new ChrArr(Paths.get(w.asString()).resolve(x.asString()).toString());
    }
  };
  
  
  public Value getv(String s) {
    switch (s) {
      case "at": return at;
    }
    throw new ValueError("No key "+s+" in •file");
  }
  
  public void setv(String s, Value v) {
    throw new DomainError("Assigning into •file");
  }
  
  private abstract class FB extends FnBuiltin {
    private final String name;
    public FB(String name) { this.name = "(file)."+name; }
    public String ln(FmtInfo f) { return name; }
    public boolean eq(Value o) { return this==o; }
  }
}