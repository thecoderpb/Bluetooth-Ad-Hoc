# Bluetooth-Ad-Hoc

Managing Connection Between Multiple Bluetooth Devices and distritbuting task among the devices.

<h4>Structure of The Network</h4>
Each device in the Bluetooth network is a node. We have two types of nodes- master
and slave. Master node is responsible for distribution of work to the slave nodes and
for connecting new devices in the network. Slave nodes are solely responsible for doing
any kind of computational work that they are assigned. 
Each cluster has one master and multiple slave nodes connected in a star topology fashion.This forms a cluster.
Multiple such clusters connected to each other via their respective master nodes forms
our Bluetooth network.
<br>While connecting, the master node ranks all its slave nodes according to its computational
power. Whenever a new connection is made by the master, the master requests
for properties from the slave device and the slave sends its device properties to the
master. The master then assigns ranks to these slaves based on their system properties.
The ranking is important as we will see that it will be useful for creating a fault
tolerant network.

<h4>Installation</h4>
Extract the source code. Place it in androidStudioProjects folder located in documents folder.<br>
Open Android Studio and open the location of the project. Let the gradle project sync. It will take a while
<br> Then connect your device and run the code.Upon successfull installation, the app will run on your device. Follow the prompts on the screen to test the app.
