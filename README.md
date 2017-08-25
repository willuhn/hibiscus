Hibiscus
========

A Java based, plattform independent (running on Linux, Windows, OS X, OpenBSD, OpenSolaris) homebanking application, that uses the 
german FinTS/HBCI standard. All data (accounts, transactions, addresses, ..) are stored encrypted into an embedded database (H2) by default or on a Mysql server (if configured).

Hibiscus runs as a plugin within the [Jameica framework](https://github.com/willuhn/jameica) as either a typical desktop application or headless in server mode.

# Developer Information

* [Issue Tracker (Bugzilla)](http://www.willuhn.de/bugzilla)
* [Developer Information (in german)](http://www.willuhn.de/products/hibiscus/dev.php)

When developing with Eclipse, you can add the following project setup in the Eclipse installer for setting up a development workspace: `https://raw.githubusercontent.com/willuhn/hibiscus/master/Hibiscus.setup`

It will clone the Jameica and Hibiscus repositories, adapt the swt classpath according to your system and create a launch config entry for starting the application.