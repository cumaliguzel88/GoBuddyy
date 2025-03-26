const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();

exports.sendNotification = functions.firestore
    .document('notifications/{notificationId}')
    .onCreate(async (snap, context) => {
        const notification = snap.data();
        const userDoc = await admin.firestore()
            .collection('users')
            .doc(notification.toUserId)
            .get();

        const fcmToken = userDoc.data()?.fcmToken;
        if (!fcmToken) return;

        const message = {
            notification: {
                title: notification.title || 'Yeni Bildirim',
                body: notification.message
            },
            data: {
                type: notification.type || 'SYSTEM',
                ...notification.data
            },
            token: fcmToken
        };

        return admin.messaging().send(message);
    }); 