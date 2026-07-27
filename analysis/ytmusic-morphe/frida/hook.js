Java.perform(function () {
  function fieldStr(obj, name) {
    try {
      var f = obj.getClass().getDeclaredField(name);
      f.setAccessible(true);
      var v = f.get(obj);
      return v === null ? 'null' : String(v);
    } catch (e) { return '<err ' + e + '>'; }
  }

  function hookAll(clsName, methodName, makeLog) {
    var cls = Java.use(clsName);
    var m = cls[methodName];
    var overloads = m.overloads;
    console.log('[setup] ' + clsName + '.' + methodName + ' overloads=' + overloads.length);
    overloads.forEach(function (ov) {
      ov.implementation = function () {
        var ret = ov.apply(this, arguments);
        try {
          console.log(makeLog(arguments, ret));
        } catch (e) {
          console.log('[' + clsName + '.' + methodName + '] ret=' + ret + ' (log err: ' + e + ')');
        }
        return ret;
      };
    });
  }

  hookAll('kxo', 'g', function (args, ret) {
    return '[kxo.g] caller_pkg=' + fieldStr(args[0], 'b') + ' => ' + ret;
  });
  hookAll('kxo', 'h', function (args, ret) {
    return '[kxo.h] caller=' + fieldStr(args[0], 'b') + ' => ' + ret;
  });
  hookAll('kxo', 'm', function (args, ret) {
    return '[kxo.m] caller_pkg=' + fieldStr(args[0], 'b') + ' => ' + ret;
  });
  hookAll('com.google.android.apps.youtube.music.mediabrowser.MusicBrowserService', 'f', function (args, ret) {
    var retCls = ret === null ? 'null' : ret.getClass().getName();
    var rootId = ret === null ? '' : (' rootId_field=' + fieldStr(ret, 'a'));
    return '[MBS.f] pkg=' + args[0] + ' => class=' + retCls + rootId;
  });
  console.log('[setup] hooks instalados');
});
