# Emperors

Inspired from @ReUnicoRebornBot concept and @RePaperaBot, Emperors is a new way to keep your group alive and active.

## 🎉 Create your own emperors

With Emperors, you can create your **own** unlimited emperors, with a photo of your choice!

here is a quick summary of the features of Emperors:

* Unlimited emperor creation
* Photo support
* All data stored in a MySQL database
* Emperors listing
* Exception handling

## 🎈 How to run Emperors locally?

**Required Dependencies:**

* Java 7 (We don't support dinosaurs here)
* Maven
* An Internet connection (Well, if you are reading this it means that you already have it 👀)
* MySQL 3.8+ Installed

Simple, just clone this repository with `git clone https://github.com/ImOnlyFire/Emperors.git`
and then build the project with `mvn clean install`. Your new jar is located at `target/EmperorsBot-$VERSION.jar`

## ⭐ Creating a clone

* First of all, you need to create a new bot from [@BotFather](https://t.me/botfather) - Make sure to disable privacy mode as well!
* Then, you need an imgur api token. To get it, follow this link from the [Imgur Docs](https://apidocs.imgur.com/#authorization-and-oauth).
* And finally, you need a mysql database connection available.

Finally go to the `config.properties` file and fill in with the bot token, bot username, database credentials and imgur token
