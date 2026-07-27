import Java from "frida-java-bridge";

function dumpObj(obj: any, depth?: number): string {
  if (obj === null || obj === undefined) return "null";
  try {
    const cls = obj.getClass();
    const parts: string[] = [cls.getName() + "{"];
    const fields = cls.getDeclaredFields();
    for (const f of fields) {
      try {
        f.setAccessible(true);
        const v = f.get(obj);
        let s: string;
        if (v === null) s = "null";
        else if (typeof v === "string") s = '"' + v + '"';
        else if (typeof v === "number" || typeof v === "boolean") s = String(v);
        else if ((depth || 0) < 1 && v.getClass && v.getClass().getName().startsWith("L")) s = String(v);
        else s = "[" + v.getClass().getName() + "]";
        parts.push(f.getName() + "=" + s);
      } catch (e) {
        parts.push(f.getName() + "=<err>");
      }
    }
    return parts.join(" ") + "}";
  } catch (e) {
    return "<dump err: " + e + ">";
  }
}

function dumpSets(self: any): string {
  const out: string[] = [];
  try {
    const fields = self.getClass().getDeclaredFields();
    for (const f of fields) {
      try {
        f.setAccessible(true);
        const v = f.get(self);
        if (v === null) continue;
        const tname = v.getClass().getName();
        if (tname.indexOf("Set") >= 0 || tname.indexOf("HashSet") >= 0) {
          const arr = v.toArray();
          const items: string[] = [];
          for (let i = 0; i < arr.length && i < 30; i++) {
            items.push(dumpObj(arr[i]));
          }
          out.push("SET " + f.getName() + " (" + v.size() + "): [" + items.join(", ") + "]");
        }
      } catch (e) { /* ignora campo */ }
    }
  } catch (e) {
    out.push("<dumpSets err: " + e + ">");
  }
  return out.join("\n");
}

function hookAll(clsName: string, methodName: string, makeLog: (thiz: any, args: any[], ret: any) => string) {
  try {
    const cls = Java.use(clsName);
    const m = (cls as any)[methodName];
    const overloads = m.overloads;
    overloads.forEach(function (ov: any) {
      ov.implementation = function () {
        const args = Array.prototype.slice.call(arguments);
        const ret = ov.apply(this, args);
        try {
          console.log(makeLog(this, args, ret));
        } catch (e) {
          console.log("[" + clsName + "." + methodName + "] ret=" + ret + " (log err: " + e + ")");
        }
        return ret;
      };
    });
    console.log("[setup] OK " + clsName + "." + methodName + " overloads=" + overloads.length);
  } catch (e) {
    console.log("[setup] FALHOU " + clsName + "." + methodName + ": " + e);
  }
}

let dumpedG = false;
let dumpedH = false;

Java.perform(function () {
  hookAll("kxo", "g", function (thiz, args, ret) {
    let extra = "";
    if (!ret && !dumpedG) {
      dumpedG = true;
      extra = "\n" + dumpSets(thiz);
    }
    return "[kxo.g] caller=" + dumpObj(args[0]) + " => " + ret + extra;
  });
  hookAll("kxo", "h", function (thiz, args, ret) {
    let extra = "";
    if (!ret && !dumpedH) {
      dumpedH = true;
      extra = "\n" + dumpSets(thiz);
    }
    return "[kxo.h] caller=" + dumpObj(args[0]) + " => " + ret + extra;
  });
  hookAll("kxo", "m", function (thiz, args, ret) {
    return "[kxo.m] caller=" + dumpObj(args[0]) + " => " + ret;
  });
  hookAll(
    "com.google.android.apps.youtube.music.mediabrowser.MusicBrowserService",
    "f",
    function (thiz, args, ret) {
      const retCls = ret === null ? "null" : ret.getClass().getName();
      return "[MBS.f] pkg=" + args[0] + " => class=" + retCls + " obj=" + dumpObj(ret);
    }
  );
  console.log("[setup] hooks instalados");
});
