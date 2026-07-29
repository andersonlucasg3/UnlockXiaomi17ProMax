import sys, time
import frida

TARGET = "com.google.android.gms"
BASE = r"c:/Users/anderson/Projetos/UnlockXiaomi/tools/frida-agent"

def main():
    src = open(BASE + "/_agent.js", encoding="utf-8").read() + "\n" + \
          open(BASE + "/gms-dck-unlock.js", encoding="utf-8").read()
    dev = frida.get_device_manager().add_remote_device("127.0.0.1:27111")
    session = dev.attach(TARGET)
    script = session.create_script(src)

    def on_msg(msg, data):
        if msg.get("type") == "send":
            print("[SCRIPT]", msg.get("payload"))
        elif msg.get("type") == "error":
            print("[ERR]", msg.get("description"))
            if msg.get("stack"):
                print(msg["stack"][:800])

    script.on("message", on_msg)
    script.load()
    print("[*] loaded into", TARGET)
    while True:
        time.sleep(3600)

if __name__ == "__main__":
    main()
