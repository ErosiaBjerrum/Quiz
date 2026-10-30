
const API = "/quizzes";

// HTML-elementer
const startScreen = document.getElementById("start-screen");
const quizScreen = document.getElementById("quiz-screen");
const resultScreen = document.getElementById("result-screen");
const lobbyScreen = document.getElementById("lobby-screen");
const startButton = document.getElementById("start-button");
const createRoomButton = document.getElementById("create-room-button");
const joinRoomButton = document.getElementById("join-room-button");
const nextButton = document.getElementById("next-button");
const restartButton = document.getElementById("restart-button");

const errorMessage = document.getElementById("error-message");

// Quizens aktuelle tilstand
let sessionId = null;
let participantToken = null;
let multiplayerSocket = null;
let isHost = false;
let answeredQuestions = 0;
let totalQuestions = 0;

// Vis kun den ønskede skærm
function showScreen(screen) {
    startScreen.hidden = screen !== startScreen;
    quizScreen.hidden = screen !== quizScreen;
    resultScreen.hidden = screen !== resultScreen;
    lobbyScreen.hidden = screen !== lobbyScreen;
    errorMessage.textContent = "";
}

// Send en request og læs svaret fra backenden
async function apiRequest(url, options = {}) {
    const response = await fetch(url, options);
    const text = await response.text();

    let data;
    try {
        data = text ? JSON.parse(text) : {};
    } catch {
        data = { msg: text };
    }

    if (!response.ok) {
        throw new Error(data.msg || `HTTP-fejl ${response.status}`);
    }

    return data;
}

// Vis fejl på siden
function showError(error) {
    errorMessage.textContent = error.message;
    console.error(error);
}

async function createMultiplayerRoom() {
    const username = document.getElementById("username").value.trim();
    const numberOfQuestions = Number(document.getElementById("question-count").value);
    const difficulty = document.getElementById("difficulty").value || null;

    if (!username) {
        errorMessage.textContent = "Skriv dit navn først.";
        return;
    }

    try {
        const result = await apiRequest("/quizzes/rooms", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                username,
                numberOfQuestions,
                difficulty,
                categoryId: null
            })
        });

        sessionId = result.sessionId;
        participantToken = result.participantToken;
        isHost = true;

        answeredQuestions = 0;
        totalQuestions = numberOfQuestions;

        document.getElementById("room-code").textContent = result.roomCode;
        document.getElementById("start-multiplayer-button").hidden = false;

        showScreen(lobbyScreen);
        await updateParticipantList();
        connectMultiplayerSocket();

    } catch (error) {
        showError(error);
    }
}

async function joinMultiplayerRoom() {
    const username = document.getElementById("username").value.trim();
    const roomCode = document.getElementById("join-code").value.trim();

    if (!username || !roomCode) {
        errorMessage.textContent = "Udfyld både navn og rumkode.";
        return;
    }

    try {
        const result = await apiRequest("/quizzes/rooms/join", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ username, roomCode })
        });

        sessionId = result.sessionId;
        participantToken = result.participantToken;
        isHost = false;
        answeredQuestions = 0;
        totalQuestions = result.totalQuestions;

        document.getElementById("join-fields").hidden = true;
        document.getElementById("room-code").parentElement.hidden = false;
        document.getElementById("room-code").textContent = result.roomCode;
        await updateParticipantList();
        connectMultiplayerSocket();

    } catch (error) {
        showError(error);
    }
}

async function updateParticipantList() {
    if (!sessionId) return;

    try {
        const participants = await apiRequest(
            `/quizzes/rooms/${sessionId}/participants`
        );

        const list = document.getElementById("participant-list");
        list.replaceChildren();

        for (const name of participants) {
            const item = document.createElement("li");
            item.textContent = name;
            list.appendChild(item);
        }

    } catch (error) {
        showError(error);
    }
}

let quizTimerInterval = null;

function startQuizTimer() {
    clearInterval(quizTimerInterval);

    let secondsLeft = 30;
    const timerDisplay = document.getElementById("quiz-timer");

    timerDisplay.textContent = `Tid tilbage: ${secondsLeft} sekunder`;

    quizTimerInterval = setInterval(() => {
        secondsLeft--;

        timerDisplay.textContent = `Tid tilbage: ${secondsLeft} sekunder`;

        if (secondsLeft <= 0) {
            clearInterval(quizTimerInterval);
        }
    }, 1000);
}

function connectMultiplayerSocket() {
    multiplayerSocket = new WebSocket(
        `ws://${window.location.host}/ws/quiz/${sessionId}`
    );

    multiplayerSocket.onmessage = event => {
        console.log("Multiplayer:", event.data);
        if (event.data === "QUIZ_FINISHED") {
            document.getElementById("final-score").textContent =
                document.getElementById("score-display").textContent;

            showScreen(resultScreen);
            return;
        }
        if (event.data.startsWith("{")) {
            const data = JSON.parse(event.data);

            if (!data.answers) {
                const scores = Object.entries(data)
                    .map(([name, score]) => `${name}: ${score} point`)
                    .join(" | ");

                document.getElementById("score-display").textContent = scores;
                return;
            }
        }
        if (event.data.startsWith("CORRECT_ANSWER:")) {
            clearInterval(quizTimerInterval);

            document.querySelectorAll("#answer-options button").forEach(button => {
                button.disabled = true;
            });

            const correctAnswer = event.data.substring("CORRECT_ANSWER:".length);

            document.getElementById("answer-feedback").textContent =
                "Korrekt svar: " + correctAnswer;
            document.getElementById("next-button").hidden = !isHost;
            document.getElementById("next-button").textContent = "Næste spørgsmål";
            return;
        }
        if (event.data.startsWith("{")) {
            const question = JSON.parse(event.data);

            if (question.answers) {
                showScreen(quizScreen);

                updateQuestionNumber();
                answeredQuestions++;

                startQuizTimer();

                document.getElementById("question-text").textContent = question.text;

                const answerOptions = document.getElementById("answer-options");
                answerOptions.replaceChildren();

                for (const answer of question.answers) {
                    const button = document.createElement("button");
                    button.textContent = answer.text;
                    button.addEventListener("click", () => {
                        multiplayerSocket.send(`ANSWER:${participantToken}:${answer.id}`);

                        answerOptions.querySelectorAll("button").forEach(btn => {
                            btn.disabled = true;
                        });

                        document.getElementById("answer-feedback").textContent =
                            "Svar registreret. Venter på de andre deltagere...";
                    });
                    answerOptions.appendChild(button);
                }
            }
        }

        if (event.data === "PARTICIPANTS_UPDATED") {
            updateParticipantList();
        }
    };
}

// Start en ny quiz
async function startQuiz() {
    const username = document.getElementById("username").value.trim();
    const numberOfQuestions =
        Number(document.getElementById("question-count").value);

    const difficulty =
        document.getElementById("difficulty").value || null;

    if (!username) {
        errorMessage.textContent = "Skriv dit navn først.";
        return;
    }

    startButton.disabled = true;
    errorMessage.textContent = "";

    try {
        const result = await apiRequest(API, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                username,
                numberOfQuestions,
                difficulty,
                categoryId: null
            })
        });

        sessionId = result.sessionId;
        answeredQuestions = 0;
        totalQuestions = numberOfQuestions;

        showScreen(quizScreen);
        document.getElementById("quiz-timer").style.display = "none";
        await loadQuestion();

    } catch (error) {
        showError(error);
    } finally {
        startButton.disabled = false;
    }
}

// Hent og vis det aktuelle spørgsmål
async function loadQuestion() {
    try {
        const question = await apiRequest(
            `${API}/${sessionId}/question`
        );

        updateQuestionNumber();

        document.getElementById("question-text").textContent =
            question.text;

        const answersContainer =
            document.getElementById("answer-options");

        answersContainer.replaceChildren();

        document.getElementById("answer-feedback").textContent = "";
        document.getElementById("answer-feedback").className = "";
        nextButton.hidden = true;

        // Opret én knap for hver svarmulighed
        for (const answer of question.answers) {
            const button = document.createElement("button");
            button.textContent = answer.text;

            button.addEventListener("click", () => {
                submitAnswer(answer.id);
            });

            answersContainer.appendChild(button);
        }

    } catch (error) {
        showError(error);
    }
}

// Indsend spillerens svar
async function submitAnswer(answerId) {
    const buttons =
        document.querySelectorAll("#answer-options button");

    // Undgå dobbeltklik og flere svar på samme spørgsmål
    buttons.forEach(button => button.disabled = true);

    try {
        const result = await apiRequest(
            `${API}/${sessionId}/answer`,
            {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ answerId })
            }
        );

        answeredQuestions++;

        const feedback =
            document.getElementById("answer-feedback");

        if (result.correct) {
            feedback.textContent =
                `Korrekt! +${result.pointsEarned} point.`;
            feedback.className = "correct";
        } else {
            feedback.textContent =
                `Forkert! Det rigtige svar er: ${result.correctAnswer}`;
            feedback.className = "incorrect";
        }

        document.getElementById("score-display").textContent =
            `Point: ${result.totalScore}`;

        nextButton.textContent =
            answeredQuestions >= totalQuestions
                ? "Se resultat"
                : "Næste spørgsmål";

        nextButton.hidden = false;

    } catch (error) {
        buttons.forEach(button => button.disabled = false);
        showError(error);
    }
}

// Gå videre eller afslut quizzen
async function nextQuestion() {
    nextButton.disabled = true;

    try {
        if (answeredQuestions >= totalQuestions) {
            await showResult();
        } else {
            await loadQuestion();
        }
    } finally {
        nextButton.disabled = false;
    }
}

// Hent slutresultatet
async function showResult() {
    try {
        const status = await apiRequest(
            `${API}/${sessionId}/status`
        );

        document.getElementById("final-score").textContent =
            `${status.username}, du fik ${status.score} ud af ` +
            `${status.maxScore} mulige point!`;

        showScreen(resultScreen);

    } catch (error) {
        showError(error);
    }
}

// Start forfra uden at genindlæse siden
function restartQuiz() {
    if (multiplayerSocket) {
        multiplayerSocket.close();
        multiplayerSocket = null;
    }

    clearInterval(quizTimerInterval);
    sessionId = null;
    answeredQuestions = 0;
    totalQuestions = 0;

    document.getElementById("score-display").textContent =
        "Point: 0";

    showScreen(startScreen);
}

function updateQuestionNumber() {
    document.getElementById("question-number").textContent =
        `Spørgsmål ${answeredQuestions + 1} af ${totalQuestions}`;
}

// Tilknyt funktionerne til knapperne
startButton.addEventListener("click", startQuiz);
nextButton.addEventListener("click", () => {
    if (multiplayerSocket) {
        multiplayerSocket.send(`NEXT:${participantToken}`);
    } else {
        nextQuestion();
    }
});
restartButton.addEventListener("click", restartQuiz);
createRoomButton.addEventListener("click", createMultiplayerRoom);
joinRoomButton.addEventListener("click", () => {
    showScreen(lobbyScreen);

    document.getElementById("participant-list").replaceChildren();

    document.getElementById("join-fields").hidden = false;
    document.getElementById("room-code").parentElement.hidden = true;
    document.getElementById("start-multiplayer-button").hidden = true;
});
document.getElementById("confirm-join-button")
    .addEventListener("click", joinMultiplayerRoom);
document.getElementById("start-multiplayer-button")
    .addEventListener("click", () => {
        multiplayerSocket.send(`START:${participantToken}`);
    });
