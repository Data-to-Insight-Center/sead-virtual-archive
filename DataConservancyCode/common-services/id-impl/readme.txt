Id Service Notes (June 3 2010)
------------------------------

This service mints new dcs identifier objects.  Currently these objects have the following properties:
- They are strongly typed using the object class they identify.
- uid - a string uid in the form "y1p:N" where N is a monotonically increasing number.
- uri - a uri object in the form "http://dataconservancy.org/<uid>"
- type - a simple String with the name of the object class being identified (obj.getClass().toString())