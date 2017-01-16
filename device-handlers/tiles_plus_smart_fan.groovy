/**
 *  Copyright 2015 ahill44
 *
 *  Custom smart fan device type with more useful tiles
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
metadata {
	definition (name: "Tiles+ Smart Fan", namespace: "ahill44", author: "Andy") {
		capability "Switch Level"
		capability "Actuator"
		capability "Indicator"
		capability "Switch"
		capability "Polling"
		capability "Refresh"
		capability "Sensor"
        
        command "speedLow"
        command "speedMedium"
        command "speedHigh"
        
        //attribute "currentState", "string"

		fingerprint inClusters: "0x26"
	}
    
	tiles(scale: 2) {
		multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.currentState", key: "PRIMARY_CONTROL") {
				attributeState "default", label: "Changing", action: "refresh.refresh", icon: "st.Lighting.light24", backgroundColor: "#2179b8", nextState: "turningOff"
				attributeState "high", label: "High", action: "switch.off", icon: "st.Lighting.light24", backgroundColor: "#486e13", nextState: "turningOff"
				attributeState "medium", label: "Medium", action: "switch.off", icon: "st.Lighting.light24", backgroundColor: "#60931a", nextState: "turningOff"
				attributeState "low", label: "Low", action: "switch.off", icon: "st.Lighting.light24", backgroundColor: "#79b821", nextState: "turningOff"
                attributeState "off", label: "Off", action: "switch.on", icon: "st.Lighting.light24", backgroundColor: "#ffffff", nextState: "turningOn"
                attributeState "turning-on", label:'${name}', action:"switch.off", icon:"st.Lighting.light24", backgroundColor:"#79b821", nextState:"turningOff"
				attributeState "turning-off", label:'${name}', action:"switch.on", icon:"st.Lighting.light24", backgroundColor:"#ffffff", nextState:"turningOn"
			}
		}
        
		standardTile("speedLow", "device.currentState", inactiveLabel: false, width: 2, height: 2) {
        	state "default", label: "Low", action: "speedLow", icon: "st.Lighting.light24", backgroundColor: "#ffffff"
			state "low", label: "Low", action: "speedLow", icon: "st.Lighting.light24", backgroundColor: "#79b821"
			state "turning-low", label: "Low", action: "speedLow", icon: "st.Lighting.light24", backgroundColor: "#2179b8"
  		}
        standardTile("speedMedium", "device.currentState", inactiveLabel: false, width: 2, height: 2) {
			state "default", label: "Medium", action: "speedMedium", icon: "st.Lighting.light24", backgroundColor: "#ffffff"
			state "medium", label: "Medium", action: "speedMedium", icon: "st.Lighting.light24", backgroundColor: "#79b821"
            state "turning-medium", label: "Medium", action: "speedMedium", icon: "st.Lighting.light24", backgroundColor: "#2179b8"
		}
		standardTile("speedHigh", "device.currentState", inactiveLabel: false, width: 2, height: 2) {
			state "default", label: "High", action: "speedHigh", icon: "st.Lighting.light24", backgroundColor: "#ffffff"
			state "high", label: "High", action: "speedHigh", icon: "st.Lighting.light24", backgroundColor: "#79b821"
            state "turning-high", label: "High", action: "speedHigh", icon: "st.Lighting.light24", backgroundColor: "#2179b8"
		}

		standardTile("indicator", "device.indicatorStatus", width: 3, height: 2, inactiveLabel: false, decoration: "flat") {
			state "when off", action:"indicator.indicatorWhenOn", icon:"st.indicators.lit-when-off"
			state "when on", action:"indicator.indicatorNever", icon:"st.indicators.lit-when-on"
			state "never", action:"indicator.indicatorWhenOff", icon:"st.indicators.never-lit"
		}
		standardTile("refresh", "device.switch", width: 3, height: 2, inactiveLabel: false, decoration: "flat") {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}

		main(["switch"])
        details(["switch", "speedLow", "speedMedium", "speedHigh", "indicator", "refresh"])
	}
    
    preferences {
		section("Fan Thresholds") {
			input "lowLevel", "number", title: "Low Level (%)", range: "1..99"
			input "mediumLevel", "number", title: "Medium Level (%)", range: "1..99"
			input "highLevel", "number", title: "High Level (%)", range: "1..99"
		}
	}
}

// Fan levels
def setSpeedValue(index) {
	Integer[] levels = [
    	((settings.lowLevel != null && settings.lowLevel != "") ? settings.lowLevel.toInteger() : 26),
        ((settings.medLevel != null && settings.medLevel != "") ? settings.medLevel.toInteger() : 41),
        ((settings.highLevel != null && settings.highLevel != "") ? settings.highLevel.toInteger() : 76)
    ]
    
    def value = ((index < levels.length) ? levels[index] : levels[0])
    // log.debug "setLevel >> " + value
    setCurrentState(true, value)
    setLevel(value)
}
def setCurrentState(pending, value) {
	// log.debug "setCurrentState(${pending}, ${value})"
    Integer[] levels = [
    	((settings.lowLevel != null && settings.lowLevel != "") ? settings.lowLevel.toInteger() : 26),
        ((settings.medLevel != null && settings.medLevel != "") ? settings.medLevel.toInteger() : 41),
        ((settings.highLevel != null && settings.highLevel != "") ? settings.highLevel.toInteger() : 76)
    ]
    
    def int currentLevel = (value == null) ? device.currentValue("level") : value
    def currentState = pending ? "turning-" : ""
    def currentSwitch = device.currentValue("switch")
    
    if (value > 0) {
    	if ((currentLevel > 0) && (currentLevel <= levels[0])) {
            currentState += "low"
        }
        else if ((currentLevel > levels[0]) && (currentLevel <= levels[1])) {
            currentState += "medium"
        }
        else if (currentLevel > levels[1]) {
            currentState += "high"
        }
    }
    else {
    	currentState += "off"
    }
    
    // log.debug "setState() >> currentLevel: ${currentLevel}"
    // log.debug "setState() >> currentSwitch: ${currentSwitch}"
    // log.debug "setState() >> currentState: ${currentState}"

    sendEvent(name: "currentState", value: currentState as String)
}
def speedLow() {
	setSpeedValue(0)
}
def speedMedium() {
	setSpeedValue(1)
}
def speedHigh() {
	setSpeedValue(2)
}
// End Fan levels

def parse(String description) {
	def result = null
	if (description != "updated") {
		log.debug "parse() >> zwave.parse($description)"
		def cmd = zwave.parse(description, [0x20: 1, 0x26: 1, 0x70: 1])
		if (cmd) {
			result = zwaveEvent(cmd)
		}
	}
	if (result?.name == 'hail' && hubFirmwareLessThan("000.011.00602")) {
		result = [result, response(zwave.basicV1.basicGet())]
		log.debug "Was hailed: requesting state update"
	} else {
		log.debug "Parse returned ${result?.descriptionText}"
	}
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
	dimmerEvents(cmd)
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd) {
	dimmerEvents(cmd)
}

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv1.SwitchMultilevelReport cmd) {
	dimmerEvents(cmd)
}

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv1.SwitchMultilevelSet cmd) {
	dimmerEvents(cmd)
}

private dimmerEvents(physicalgraph.zwave.Command cmd) {
	def value = (cmd.value ? "on" : "off")
	def result = [createEvent(name: "switch", value: value)]
	if (cmd.value && cmd.value <= 100) {
		result << createEvent(name: "level", value: cmd.value, unit: "%")
	}
    
    setCurrentState(false, cmd.value)
    
	return result
}


def zwaveEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport cmd) {
	log.debug "ConfigurationReport $cmd"
	def value = "when off"
	if (cmd.configurationValue[0] == 1) {value = "when on"}
	if (cmd.configurationValue[0] == 2) {value = "never"}
	createEvent([name: "indicatorStatus", value: value])
}

def zwaveEvent(physicalgraph.zwave.commands.hailv1.Hail cmd) {
	createEvent([name: "hail", value: "hail", descriptionText: "Switch button was pressed", displayed: false])
}

def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd) {
	log.debug "manufacturerId:   ${cmd.manufacturerId}"
	log.debug "manufacturerName: ${cmd.manufacturerName}"
	log.debug "productId:        ${cmd.productId}"
	log.debug "productTypeId:    ${cmd.productTypeId}"
	def msr = String.format("%04X-%04X-%04X", cmd.manufacturerId, cmd.productTypeId, cmd.productId)
	updateDataValue("MSR", msr)
	createEvent([descriptionText: "$device.displayName MSR: $msr", isStateChange: false])
}

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv1.SwitchMultilevelStopLevelChange cmd) {
	[createEvent(name:"switch", value:"on"), response(zwave.switchMultilevelV1.switchMultilevelGet().format())]
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	// Handles all Z-Wave commands we aren't interested in
	[:]
}

def on() {
	delayBetween([
			zwave.basicV1.basicSet(value: 0xFF).format(),
			zwave.switchMultilevelV1.switchMultilevelGet().format()
	],5000)
}

def off() {
	delayBetween([
			zwave.basicV1.basicSet(value: 0x00).format(),
			zwave.switchMultilevelV1.switchMultilevelGet().format()
	],5000)
}

def setLevel(value) {
	log.debug "setLevel >> value: $value"
	def valueaux = value as Integer
	def level = Math.max(Math.min(valueaux, 99), 0)
	if (level > 0) {
		sendEvent(name: "switch", value: "on")
	} else {
		sendEvent(name: "switch", value: "off")
	}
	sendEvent(name: "level", value: level, unit: "%")
	delayBetween ([zwave.basicV1.basicSet(value: level).format(), zwave.switchMultilevelV1.switchMultilevelGet().format()], 5000)
}

def setLevel(value, duration) {
	log.debug "setLevel >> value: $value, duration: $duration"
	def valueaux = value as Integer
	def level = Math.max(Math.min(valueaux, 99), 0)
	def dimmingDuration = duration < 128 ? duration : 128 + Math.round(duration / 60)
	def getStatusDelay = duration < 128 ? (duration*1000)+2000 : (Math.round(duration / 60)*60*1000)+2000
	delayBetween ([zwave.switchMultilevelV2.switchMultilevelSet(value: level, dimmingDuration: dimmingDuration).format(),
				   zwave.switchMultilevelV1.switchMultilevelGet().format()], getStatusDelay)
}

def poll() {
	zwave.switchMultilevelV1.switchMultilevelGet().format()
}

def refresh() {
	log.debug "refresh() is called"
	def commands = []
	commands << zwave.switchMultilevelV1.switchMultilevelGet().format()
	if (getDataValue("MSR") == null) {
		commands << zwave.manufacturerSpecificV1.manufacturerSpecificGet().format()
	}
	delayBetween(commands,100)
}

def indicatorWhenOn() {
	sendEvent(name: "indicatorStatus", value: "when on")
	zwave.configurationV1.configurationSet(configurationValue: [1], parameterNumber: 3, size: 1).format()
}

def indicatorWhenOff() {
	sendEvent(name: "indicatorStatus", value: "when off")
	zwave.configurationV1.configurationSet(configurationValue: [0], parameterNumber: 3, size: 1).format()
}

def indicatorNever() {
	sendEvent(name: "indicatorStatus", value: "never")
	zwave.configurationV1.configurationSet(configurationValue: [2], parameterNumber: 3, size: 1).format()
}

def invertSwitch(invert=true) {
	if (invert) {
		zwave.configurationV1.configurationSet(configurationValue: [1], parameterNumber: 4, size: 1).format()
	}
	else {
		zwave.configurationV1.configurationSet(configurationValue: [0], parameterNumber: 4, size: 1).format()
	}
}
