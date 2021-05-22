var http = require("http");
var mqtt = require('mqtt');
const url = require('url');

let options = {username: '', password: '', reconnectPeriod: 100};
var client = mqtt.connect("mqtt://localhost", options);

devs = {
    "desk lights": {'switch/main': ['3']},
    "desklights": {'switch/main': ['3']},
    "desk light": {'switch/main': ['3']},
    "desklight": {'switch/main': ['3']},
    "fan": {'switch/main': ['2']},
    "back lights": {'switch/main': ['1']},
    "backlights": {'switch/main': ['1']},
    "back light": {'switch/main': ['1']},
    "backlight": {'switch/main': ['1']},
    "cfl light": {'switch/main': ['0']},
    "mains socket": {'switch/extra': ['0']},
    "ups socket": {'switch/extra': ['1']},
    "single light": {'switch/extra': ['2']},
    "outer bulb": {'switch/extra': ['3']},
    "all lights": {'switch/main': ['0', '1', '3'], 'switch/extra': ['2']},
    "all systems": {'switch/main': ['0', '1', '2', '3'], 'switch/extra': ['2', '3']}
};
states = ['0', '1'];

function sendPayload(dev, state) {
    let response = {Result: "Success", StatusCode: 200, Message: "Status Updated"};

    let data = devs[dev]

    for (let x of Object.keys(data)) {
        let topic = x;
        let switches = data[x].concat(new Array(data[x].length).fill(state));
        let payload = switches.join('');

        client.publish(topic, payload, (err) => {
            if (err) {
                client = mqtt.connect("mqtt://klinux.ml", options);
                response.Result = "Failed";
                response.StatusCode = 500;
                response.Message = err.toString();
                response.Message += " -> Initiating client connection... Please Retry";
                return response;
            }
        });
    }
    return response;
}

function processEvent(event) {
    let response = {Result: "Failed", StatusCode: 400, Message: "Unknown Error"};

    let result = event.split('/');
    if (result.length === 2) {
        let dev = result[0], state = result[1];
        if (Object.keys(devs).includes(dev))
            if (states.includes(state))
                response = sendPayload(dev, state);
            else
                response.Message = "Invalid Status Code";
        else
            response.Message = "Please register this device";
    } else
        response.Message = "Failed : Invalid Event";

    return response;
}

http.createServer(function (request, response) {
    console.log(request.url);

    let result = {Result: "Failed", StatusCode: 405, Message: "Event not specified"};
    const query = url.parse(request.url, true).query;

    if (query.event) {
        result = processEvent(query.event);
        result.Event = query.event;
    }

    response.writeHead(result.StatusCode, {'Content-Type': 'application/json'});
    delete result.StatusCode;
    response.end(JSON.stringify(result));

}).listen(8585);

console.log('Server running at http://127.0.0.1:8585/');
