importPackage(net.java.btrace.visualvm.tracer.deployer);

var loc = new L11N("com.sun.tools.visualvm.modules.tracer.javafx")

var scriptPath = "nbres:/com/sun/tools/visualvm/modules/tracer/javafx/resources/JavaFXTracer.probe"
var btraceDeployer = BTraceDeployer.instance()

VisualVM.Tracer.addPackages({
    // JavaFX Metrics package
    name: loc.getMessage("VisualVM/Tracer/packages/jfx"),
    validator: function() {
        var jvm = Packages.com.sun.tools.visualvm.application.jvm.JvmFactory.getJVMFor(application);
        return jvm != undefined && jvm.getMainClass() == "com.sun.javafx.runtime.Main";
    },
    probes: [
        {
            // FX Metrics
            name: loc.getMessage("VisualVM/Tracer/packages/jfx/probes/metrics"),
            desc: "Monitors Invalidation Rate and Replacement Rate",
            deployment: {
                deployer: btraceDeployer,
                fragment: "metrics",
                script: scriptPath
            },
            properties: [
                {
                    // invalidation rate
                    name: loc.getMessage("VisualVM/Tracer/packages/jfx/probes/metrics/properties/invalidationRate"),
                    desc: "Monitors number of invalidations per second",
                    value: mbeanAttribute("btrace:name=FxBtraceTracker", "invalidationRate")
                },
                {
                    // replacement rate
                    name: loc.getMessage("VisualVM/Tracer/packages/jfx/probes/metrics/properties/replacementRate"),
                    desc: "Monitors number of replacements per second",
                    value: mbeanAttribute("btrace:name=FxBtraceTracker", "replacementRate")
                }
            ]
        },
        {
            // FX Objects
            name: loc.getMessage("VisualVM/Tracer/packages/jfx/probes/objects"),
            desc: "Monitors Overall Rate and Hot Class Rate",
            deployment: {
                deployer: btraceDeployer,
                fragment: "objects",
                script: scriptPath
            },
            properties: [
                {
                    // Overall Rate
                    name: loc.getMessage("VisualVM/Tracer/packages/jfx/probes/objects/properties/fxObjectCreationRate"),
                    desc: "Monitors the number of created objects per second",
                    value: mbeanAttribute("btrace:name=FxBtraceTracker", "fxObjectCreationRate")
                }
            ]
        },
        {
            // Average FPS
            name: loc.getMessage("VisualVM/Tracer/packages/jfx/probes/fps"),
            desc: "Monitors average frame per second rate",
            deployment: {
                deployer: btraceDeployer,
                fragment: "fps",
                script: scriptPath
            },
            properties: [
                {
                    // Average FPS
                    name: loc.getMessage("VisualVM/Tracer/packages/jfx/probes/fps/properties/averageFPS"),
                    desc: "Monitors average frame per second rate",
                    value: mbeanAttribute("btrace:name=FxBtraceTracker", "averageFPS")
                }
            ]
        },
        {
            // Scenegraph mouse and key statistics
            name: loc.getMessage("VisualVM/Tracer/packages/jfx/probes/pulseCount"),
            desc: "Monitors mouse and keyboard activity",
            deployment: {
                deployer: btraceDeployer,
                fragment: "pulseCount",
                script: scriptPath
            },
            properties: [
                {
                    // Key events
                    name: loc.getMessage("VisualVM/Tracer/packages/jfx/probes/pulseCount/properties/keyPulses"),
                    desc: "Monitors keyboard activity",
                    value: mbeanAttribute("btrace:name=FxBtraceTracker", "keyPulses")
                },
                {
                    // Mouse events
                    name: loc.getMessage("VisualVM/Tracer/packages/jfx/probes/pulseCount/properties/mousePulses"),
                    desc: "Monitors mouse activity",
                    value: mbeanAttribute("btrace:name=FxBtraceTracker", "mousePulses")
                }
            ]
        },
        {
            // Scenegraph mouse and key event timing
            name: loc.getMessage("VisualVM/Tracer/packages/jfx/probes/pulseTiming"),
            desc: "Monitors mouse and keyboard event timing",
            deployment: {
                deployer: btraceDeployer,
                fragment: "pulseTiming",
                script: scriptPath
            },
            properties: [
                {
                    // Key events time
                    name: loc.getMessage("VisualVM/Tracer/packages/jfx/probes/pulseTiming/properties/keyPulsesCumulativeTime"),
                    desc: "Monitors keyboard processing time",
                    value: mbeanAttribute("btrace:name=FxBtraceTracker", "keyPulsesCumulativeTime")
                },
                {
                    // Mouse events time
                    name: loc.getMessage("VisualVM/Tracer/packages/jfx/probes/pulseTiming/properties/mousePulsesCumulativeTime"),
                    desc: "Monitors mouse processing time",
                    value: mbeanAttribute("btrace:name=FxBtraceTracker", "mousePulsesCumulativeTime")
                }
            ]
        },
        {
            // Scenegraph timing monitoring
            name: loc.getMessage("VisualVM/Tracer/packages/jfx/probes/sgTiming"),
            desc: "Monitors scenegraph timing",
            deployment: {
                deployer: btraceDeployer,
                fragment: "sgTiming",
                script: scriptPath
            },
            properties: [
                {
                    // Dirty regions
                    name: loc.getMessage("VisualVM/Tracer/packages/jfx/probes/sgTiming/properties/dirtyRegionsCumulativeTime"),
                    desc: "Dirty regions processing time",
                    value: mbeanAttribute("btrace:name=FxBtraceTracker", "dirtyRegionsCumulativeTime")
                },
                {
                    // Paint time
                    name: loc.getMessage("VisualVM/Tracer/packages/jfx/probes/sgTiming/properties/paintCumulativeTime"),
                    desc: "Paint processing time",
                    value: mbeanAttribute("btrace:name=FxBtraceTracker", "paintCumulativeTime")
                },
                {
                    // Synchronization time
                    name: loc.getMessage("VisualVM/Tracer/packages/jfx/probes/sgTiming/properties/synchronizationTime"),
                    desc: "Synchronization time",
                    value: mbeanAttribute("btrace:name=FxBtraceTracker", "synchronizationTime")
                }
            ]
        },
        {
            // Scenegraph nodes statistics
            name: loc.getMessage("VisualVM/Tracer/packages/jfx/probes/sgNode"),
            desc: "Scenegraph nodes statistic",
            deployment: {
                deployer: btraceDeployer,
                fragment: "sgNode",
                script: scriptPath
            },
            properties: [
                {
                    // Layout required
                    name: loc.getMessage("VisualVM/Tracer/packages/jfx/probes/sgNode/properties/needsLayout"),
                    desc: "How many nodes layout required",
                    value: mbeanAttribute("btrace:name=FxBtraceTracker", "needsLayout")
                },
                {
                    // Node count
                    name: loc.getMessage("VisualVM/Tracer/packages/jfx/probes/sgNode/properties/nodeCount"),
                    desc: "Amount of nodes in sceengraph",
                    value: mbeanAttribute("btrace:name=FxBtraceTracker", "nodeCount")
                }
            ]
        },
        {
            // Scenegraph CSS statistics
            name: loc.getMessage("VisualVM/Tracer/packages/jfx/probes/sgCss"),
            desc: "Scenegraph CSS statistic",
            deployment: {
                deployer: btraceDeployer,
                fragment: "sgCss",
                script: scriptPath
            },
            properties: [
                {
                    // Style helpers calls
                    name: loc.getMessage("VisualVM/Tracer/packages/jfx/probes/sgCss/properties/getStyleHelperCalls"),
                    desc: "Style helpers calls",
                    value: mbeanAttribute("btrace:name=FxBtraceTracker", "getStyleHelperCalls")
                },
                {
                    // Style helpers count
                    name: loc.getMessage("VisualVM/Tracer/packages/jfx/probes/sgCss/properties/styleHelperCount"),
                    desc: "Style helpers count",
                    value: mbeanAttribute("btrace:name=FxBtraceTracker", "styleHelperCount")
                },
                {
                    // Style helpers count
                    name: loc.getMessage("VisualVM/Tracer/packages/jfx/probes/sgCss/properties/transitionToStateCalls"),
                    desc: "Transitions",
                    value: mbeanAttribute("btrace:name=FxBtraceTracker", "transitionToStateCalls")
                },
                {
                    // Style helpers count
                    name: loc.getMessage("VisualVM/Tracer/packages/jfx/probes/sgCss/properties/processCssCount"),
                    desc: "CSS process calls",
                    value: mbeanAttribute("btrace:name=FxBtraceTracker", "processCssCount")
                }
            ]
        },
        {
            // Synchronization calls
            name: loc.getMessage("VisualVM/Tracer/packages/jfx/probes/synCalls"),
            desc: "Monitors amount of synchronization calls",
            deployment: {
                deployer: btraceDeployer,
                fragment: "synCalls",
                script: scriptPath
            },
            properties: [
                {
                    // Synchronization calls
                    name: loc.getMessage("VisualVM/Tracer/packages/jfx/probes/synCalls/properties/synchronizationCalls"),
                    desc: "Monitors amount of synchronization calls",
                    value: mbeanAttribute("btrace:name=FxBtraceTracker", "synchronizationCalls")
                }
            ]
        },
    ]
})