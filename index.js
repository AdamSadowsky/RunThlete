import { initializeApp } from 'firebase-admin/app';
import { getFirestore } from 'firebase-admin/firestore';
import { DateTime } from 'luxon';
initializeApp();
const db = getFirestore();
const now = DateTime.now().setZone('America/New_York');
const dailyId = now.toFormat('yyyy-LL-dd');
const weeklyId = now.toFormat("yyyy-'W'WW");
const monthlyId = now.toFormat("yyyy-MM");

async function getTop(path, field = 'totalDistance', limit = 100) {
    const snap = await db.collection(path).orderBy(field, 'desc').limit(limit).get();
    return snap.docs.map(d => ({userId: d.id, ...d.data()}));
}

async function main() {
    const writer = db.bulkWriter();
    const paths = [`dailyTotals/${dailyId}/users`, `weeklyTotals/${weeklyId}/users`, `monthlyTotals/${monthlyId}/users`];

    const [daily, weekly, monthly] = await Promise.all(
    paths.map(p => getTop(p))
  );
    
    writer.set(db.doc('leaderboards/daily'), {
        updatedAt: now.toJSDate(), 
        entries: daily, 
    }, {merge: true});

    writer.set(db.doc('leaderboards/weekly'), {
        updatedAt: now.toJSDate(), 
        entries: weekly, 
    }, {merge: true});

    writer.set(db.doc('leaderboards/monthly'), {
        updatedAt: now.toJSDate(), 
        entries: monthly, 
    }, {merge: true});

    await writer.close();

    console.log('Leaderboards refreshed:', now.toISO());
}

main().catch(err => {
        console.error(err);
        process.exit(1);
    });
    