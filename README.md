# impulse meta

[**impulse**](https://toem.io/index.php/products/impulse) is a powerful visualization and analysis workbench designed to help engineers understand, analyze, and debug complex semiconductor and multi-core software systems. It provides a seamless integration into IDE frameworks and supports a growing set of extensions, such as IoT, language IDEs, and reporting tools. 

With its client-server architecture, **impulse** divides workloads between signal-providing tasks (on a simulation or application server) and local analysis tools, ensuring efficient performance. Engineers can use their favorite web IDE to analyze logs, traces, and simulation data without transferring large amounts of data or relying on local tools.

**impulse**’s open extension mechanisms allow users to adapt it to their specific needs. Whether defining custom data formats, implementing acquisition interfaces, creating specialized diagrams, or integrating a 3D chart engine, **impulse** offers unparalleled flexibility. Its lightweight frontend and powerful Java backend enable the creation of high-performance, complex analysis applications.

> **Note:**  
> This is a meta repository containing documentation, interfaces and generative core parts for impulse.  
> For usage and installation, please refer to platform-specific repositories such as [impulse.vscode](https://github.com/toem/impulse.vscode).

## Features

impulse offers a wide range of data formats and external interfaces through signal adaptors, providing a unified view of various data sources. Whether reading signals from log, wave, or trace files, or accessing live data streams, users can seamlessly integrate data from multiple channels. Signal adaptors allow direct connections to sources such as TCP streams, pipes, applications, external networks, serial interfaces, and data acquisition units. When dealing with multiple signal sources—such as log data from a serial interface and trace data over TCP—impulse can merge and synchronize them, ensuring accurate, comprehensive analysis across diverse systems and interfaces.

### Data Integration
- Supports log, wave, trace files, and live data streams.
- Enables connections to diverse data sources via signal adaptors including TCP streams, pipes, serial interfaces, and hardware.
- Synchronizes multiple signal sources for unified analysis.

### Customizable Views
- Create tailored views with structured navigation.
- Switch between views for efficient signal interpretation.
- Use diagrams to visualize signals across domains like time and frequency.

### Signal Processing
- Combine signals using mathematical operations.
- Decode data with protocol parsers.
- Generate reference signals and extract statistical insights.

### Sample Tables
- Tabular representation synchronized with viewers.
- Real-time updates for streaming data.
- Configurable columns for custom value representation.

### Automation
- Background processing of large datasets.
- Automatic conflict detection and pattern identification.
- Statistical extraction for performance insights.

## License

**Our guiding principle for this and all subsequent versions is:**

* Non-commercial use is free.
* Commercial use requires a commercial license key.
* This beta version does not require an license keys.

see [Licensing options](https://toem.io/index.php/pricing)

## Requirements

- impulse requires a java Runtime environment (at least JRE8). For communication to the osgi impulse server, a socket interface is used.

## Documentation
 
Enter [https://toem.io/index.php/resources](https://toem.io/index.php/resources) for more information about impulse. 


## Issues and Proposals

Use [https://github.com/toem/impulse/issues](https://github.com/toem/impulse/issues) for issues and proposals.

You may use [support@toem.de](mailto:support@toem.de) for further information.

