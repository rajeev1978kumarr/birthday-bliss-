const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const DATABASE_ID = "ai-studio-android-birthday-e14fbf3e-bfb8-45b4-a9c9-b7edcf8bc07a";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function validCardPayload(uid, extra = {}) {
  return {
    userId: uid,
    recipientName: "Priya",
    senderName: "Aarav",
    milestone: "25th",
    greetingTitle: "Happy Birthday!",
    message: "Wishing you endless happiness!",
    themeStyle: "GOLDEN_CONFETTI",
    photoFrameShape: "POLAROID",
    stickerAccent: "CAKE",
    letterSalutation: "Dear Priya,",
    letterBody: "Have the best year ever!",
    letterClosing: "With love,",
    giftSurprise: "Dinner Treat!",
    quizQuestions: [
      {
        id: "q1",
        question: "Favorite cake?",
        options: ["Chocolate", "Vanilla"],
        correctIndex: 0,
        revealNote: "Always chocolate!",
      },
    ],
    createdAt: new Date(),
    updatedAt: new Date(),
    ...extra,
  };
}

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user cannot read or list cards", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore(DATABASE_ID);
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("cards").get());
});

test("Authenticated owner can create, read, update, and delete their birthday card", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore(DATABASE_ID);
  const cardRef = aliceDb.collection("users").doc(ALICE_UID).collection("cards").doc("card_1");
  await assertSucceeds(cardRef.set(validCardPayload(ALICE_UID)));
  await assertSucceeds(cardRef.get());
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).collection("cards").get());
  await assertSucceeds(cardRef.update({ message: "Updated wish!", updatedAt: new Date() }));
  await assertSucceeds(cardRef.delete());
});

test("Cross-user access to another user's birthday card fails", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore(DATABASE_ID);
  const aliceCard = aliceDb.collection("users").doc(ALICE_UID).collection("cards").doc("card_1");
  await assertSucceeds(aliceCard.set(validCardPayload(ALICE_UID)));

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore(DATABASE_ID);
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("cards").doc("card_1").get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("cards").get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("cards").doc("card_2").set(validCardPayload(ALICE_UID)));
});

test("Shadow update with ghost field is rejected", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore(DATABASE_ID);
  const cardRef = aliceDb.collection("users").doc(ALICE_UID).collection("cards").doc("card_ghost");
  await assertFails(cardRef.set(validCardPayload(ALICE_UID, { isAdmin: true })));
});

test("Authenticated owner can write and update game scores, cross-user fails", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore(DATABASE_ID);
  const scoreRef = aliceDb.collection("users").doc(ALICE_UID).collection("gameScores").doc("balloon_pop");
  await assertSucceeds(scoreRef.set({
    userId: ALICE_UID,
    gameId: "balloon_pop",
    bestScore: 45,
    gamesPlayed: 1,
    createdAt: new Date(),
    updatedAt: new Date(),
  }));

  await assertSucceeds(scoreRef.update({
    bestScore: 60,
    gamesPlayed: 2,
    updatedAt: new Date(),
  }));

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore(DATABASE_ID);
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("gameScores").doc("balloon_pop").get());
});
