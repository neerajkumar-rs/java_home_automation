# Home Automation Demo

A small Spring Boot home automation dashboard using SQLite and plain HTML/CSS/JavaScript.

## Requirements

- Java 17+
- Maven 3.9+

## Run

```bash
mvn spring-boot:run
```

Open http://localhost:8080 in a browser. The SQLite database is created as `home_automation.db` in the project directory.

## Virtual device simulator

With the application running, open a second PowerShell terminal and run:

```powershell
.\scripts\virtual-devices.ps1
```

The simulator randomly changes active switch, knob, and slider devices through the REST API and prints every virtual hardware action to the terminal. You can change the interval or server URL:

```powershell
.\scripts\virtual-devices.ps1 -IntervalSeconds 2 -BaseUrl http://localhost:8080
```

Use the dashboard to add devices, deactivate them, filter the list, or remove them. Deactivated devices are ignored by the simulator.

## API

- `GET /devices` lists the current state of every device.
- `POST /device/light/on` and `/device/light/off`
- `POST /device/fan/on` and `/device/fan/off`
- `POST /device/ac/on` and `/device/ac/off`
- `POST /device` creates a device with `name` and `controlType` (`SWITCH`, `SLIDER`, `RGB`, or `SENSOR`). Sensor devices may also provide `sensorType` (`light`, `wind`, `temperature`, or `motion`).
- `POST /device/{id}/state` updates a device with `state` and `value`; RGB devices accept `red`, `green`, and `blue` values from 0 to 255.
- `POST /device/{id}/activate` and `/device/{id}/deactivate`
- `DELETE /device/{id}` removes a device.
