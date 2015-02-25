namespace java io.golgi.example.golgiedison.gen
namespace javascript GolgiEdison

struct Digital {
    1:required i32 pin,
    2:required i32 value
}

struct Analog {
    1:required i32 pin,
    2:required i32 value
}

service GolgiEdison {
    void digitalWrite(1:Digital digital),
    void digitalRead(1:Digital digital),
    void analogRead(1:Analog analog)
}

