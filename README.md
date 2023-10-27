# BioMachina

### Features
- In-Game Vehicle Part Editor
- Low-Latency & Interpolated Vehicle Animations
- Customisable Vehicle Stats and Addons
- Custom JSON Model Support

### Technologies
- JSON Serialisation and Deserialisation via Kotlinx Serialisation
- SQLite Database Implementation via Jetbrains Exposed
- Asynchronous and Concurrent Thread Execution via Kotlin Coroutines
- Optimised Vector & Quaternion Calculations to handle Vehicle Animations and Movement via Asynchronous Ticker
- Asynchronous Packet Handling and Manipulation via ProtocolLib

### Overview
BioMachina is a WIP Minecraft plugin that utilises a number of complex feature techniques to implement driveable vehicles to Minecraft without the use of mods. Meaning a player can simply join the server and drive a car for example without the need to install anything onto their client (other than a resource pack which is done automatically anyway)  

BioMachina represented many significant challenges due to the intended feature, that is to implement the ability to configure any number of vehicles of varying sizes and models and have all of them be drivable, customisable and actually ‘feel’ like you’re driving a vehicle.  

### FAQ

**How do we allow a player to control a vehicle similar to how most other ‘driving games’ work?**  
Use an invisible boat since boats are the closest thing in the base game to how a car would drive, and mount a display entity with a specified offset such that it looks like the player is driving the vehicle.  

**How do we make the vehicle rotate smoothly with the orientation of the boat?**  
Normally, any entity in a vehicle rotates with the boat’s orientation, however I found that doing this caused the model to glitch out and randomly flip. So the solution was to prevent the model from rotating naturally, and instead use a display entity's transformation function to smoothly rotate the model itself and not the entity hence resulting in a smooth animation.  

**How do we make the vehicle’s individual wheels rotate and turn based on the player’s input?**  
Listen to a player’s input packet and map those to an atomic variable which is then read by a vehicle every time it is ticked. Based on this information and the speed of the vehicle, a separate asynchronous executor exists to tick the animations for a vehicle. This executor calculates the steering angle, and rotation speed of the wheels asynchronously before sending packets to all viewing players to show the animation.  

**How do we make a vehicle automatically scale up and down terrain?**  
The solution to this problem similarly came with choosing a boat as the ‘driveable’ vehicle. A boat without gravity, means it simply floats but can still be moved on a flat plane. The solution to this was creating an executable tick-cycle where the vehicle would perform a ray trace for each wheel determining the hit location. Basic trigonometry is used to determine the difference in wheel height, the angle of elevation which is then compared with the configured limits of the vehicle. Based on all this data, the velocity of the boat itself is modified alongside the animations of the model to produce a smooth vehicle which traverses terrain incredibly naturally.  

BioMachina is definitely my most ambitious project yet.  
