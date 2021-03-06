import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class IoTSensorDeviceHandler {

    private List<Device> devices;
    private List<Sensor> sensors;

    private static final Action GET_ACTION = new Action();

    private static IoTSensorDeviceHandler instance;
    private IoTSensorDeviceHandler() {

    }

    public static IoTSensorDeviceHandler getInstance() {
        if (instance == null) {
            instance = new IoTSensorDeviceHandler();
        }
        return instance;
    }

    public void getInfo() {
        try {
            String result = ActionExecutor.getInstance().executeAction(GET_ACTION);
            parseDevice(result);
            //System.out.println("\n\n\n\n\n");

            parseSensor(result);

            //System.out.println("\n\n\n\n\n");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateDevices(List<Device> newInfo) {
        for (Device device : newInfo) {
            int index = devices.indexOf(device);
            if (index != -1) {
                devices.get(index).copyInfoToThis(device);
            } else {
                devices.add(device);
            }
        }
    }

    public void updateSensors(List<Sensor> newInfo) {
        for (Sensor sensor : newInfo) {
            int index = sensors.indexOf(sensor);
            if (index != -1) {
                sensors.get(index).copyInfoToThis(sensor);
            } else {
                sensors.add(sensor);
            }
        }
    }

    public String getDeviceAndSensorInfo() {
        getInfo();
        StringBuilder builder = new StringBuilder();
        devices.forEach(device -> builder.append(device.toString()).append("\n"));
        builder.append("P%").append(SensorHeader.getInstance().getHeaders()).append("\n");
        sensors.forEach(sensor -> builder.append(sensor.toString()).append("\n"));
        builder.deleteCharAt(builder.length() -1);
        return builder.toString();
    }

    private int parseNumberOfDevice(String line) {
        //System.out.println("trying parse number of devices from line " + line);
        String intParse = line.substring(line.lastIndexOf(" ")).trim();
        //System.out.println("Trying to parse int " + intParse);
        return Integer.parseInt(intParse);
    }

    private void parseDevice(String result) throws Exception {
        String[] devicePart = result.split("SENSORS:");
        String[] lines = devicePart[0].split("\n");
        int numberOfDevice = parseNumberOfDevice(lines[0]);
        //System.out.println("Number of devices = " + numberOfDevice);
        List<Device> tempList = new ArrayList<>(numberOfDevice);
        for(int i = 1; i <= numberOfDevice; i++) {
            tempList.add(extractDevice(lines[i]));
        }
        if (devices != null) {
            updateDevices(tempList);
        } else {
            devices = new CopyOnWriteArrayList<>(tempList);
        }
        //System.out.println("====Devices====");
        //devices.forEach(System.out::println);
        //System.out.println("====Devices====");
    }

    private Device extractDevice(String line) throws Exception {
        String[] cols = line.split("\t");
        //System.out.println("trying to parse id " + cols[0]);
        int id = Integer.parseInt(cols[0]);
        return new Device(id, State.stringToState(cols[2]), cols[1]);
    }

    private Sensor extractSensor(String line) throws Exception {
        String[] cols = line.split("\t");
        for(int i = 0; i < cols.length; i++) {
            cols[i] = cols[i].trim();
        }
        //System.out.println("trying to parse sensor id = " + cols[2]);
        int id = Integer.parseInt(cols[2]);
        return new Sensor(id, cols);
    }

    public String getTemperature() {
        if (sensors == null) {
            getInfo();
        }
        for (Sensor sensor : sensors) {
            if (sensor.getId() == 135) {
                return sensor.getValueOfField(SensorHeader.SensorField.TEMP);
            }
        }
        return null;
    }

    private void parseSensor(String result) throws Exception {
        String sensorPart = result.split("SENSORS:")[1];
        String[] lines = sensorPart.split("\n");
       // System.out.println("Sensor part = " + Arrays.toString(lines[3].split("\t")));
        int numberOfSensors = lines.length - 3;
        //System.out.println("number of sensors = " + numberOfSensors);
        List<Sensor> tempSensors = new ArrayList<>(numberOfSensors);
        for(int i = 3; i < 3 + numberOfSensors; i++) {
            tempSensors.add(extractSensor(lines[i]));
        }
        if (sensors == null) {
            sensors = new CopyOnWriteArrayList<>(tempSensors);
        } else {
            updateSensors(tempSensors);
        }
        //System.out.println("====Sensors====");
        //sensors.forEach(System.out::println);
        //System.out.println("====Sensors====");
    }

    /*public static void main(String[] args) {
        TestClient.password = args[0];
        getInstance().getInfo();
    }*/

}
