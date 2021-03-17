const functions = require("firebase-functions");
const admin = require("firebase-admin");

admin.initializeApp(functions.config().firebase);

// Create and Deploy Your First Cloud Functions
// https://firebase.google.com/docs/functions/write-firebase-functions
exports.sendNotification = functions.database.ref("/messages/{chatId}/{msgId}")
    .onCreate((snapshot, context) => {
      const chatId = context.params.chatId;
      const senderId = snapshot.val().senderId;
      const msg = snapshot.val().msg;
      const senderName=snapshot.val().senderName;
      const imageUrl=snapshot.val().imageUrl;
      const type=snapshot.val().type;
      console.log(senderName, imageUrl);
      const userId = chatId.replace(senderId, "");
      if (type =="TEXT") {
        return admin.firestore().collection("users")
            .doc(userId).get().then((doc) => {
              const token=doc.data().deviceToken;
              const payload={
                notification: {
                  title: senderName+" sent you a message",
                  body: msg,
                  clickAction: "MainActivity",
                },
              };
              return admin.messaging().sendToDevice(token, payload)
                  .then((response)=>{
                  });
            });
      } else if (type == "IMAGE") {
        return admin.firestore().collection("users")
            .doc(userId).get().then((doc) => {
              const token=doc.data().deviceToken;
              const payload={
                notification: {
                  title: senderName+" sent you image",
                  body: "Image",
                  image: msg,
                  clickAction: "MainActivity",
                },
              };
              return admin.messaging().sendToDevice(token, payload)
                  .then((response)=>{
                  });
            });
      }
    });
