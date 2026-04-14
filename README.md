# 🧩 Sudoku — Puzzle Game with Solver, Validator & Difficulty Levels

A full-featured **Java Swing Sudoku Application** with:
- 🎯 Puzzle Generator (4 Difficulty Levels)
- 🤖 Backtracking Solver
- ✅ Real-time Validator
- 📝 Notes (Pencil Mode)
- ⏱️ Timer + Mistake Counter
- 🏆 Leaderboard (MySQL)

---

## 👨‍💻 Team

| Name | Reg No |
|------|--------|
| Jiban Pradip Sahu | 250301120282 |
| Adarsh K. Tiwari | 250301120292 |
| Jagadananda Panda | 250301120283 |
| Biswajit Gantayat | 250301120264 |

---

## 🚀 Features

### 🎮 Game Mode
- 4 Difficulty Levels: Easy, Medium, Hard, Expert
- Unique Sudoku generation (Backtracking)
- Timer + Mistake limit (max 5)
- Notes mode ✏️
- Hint system 💡
- Auto-solve 🤖

### 🔍 Validator Mode
- Enter any Sudoku board
- Detect row, column, box errors
- Highlights invalid cells 🔴

### 🏆 Leaderboard
- Stores top scores in MySQL
- Filter by difficulty

---

## 🧠 Algorithms Used

- Backtracking (Solver)
- Randomized Backtracking (Generator)
- HashSet Validation (O(1) duplicate detection)
- Unique Solution Checker

---

## 🛠️ Tech Stack

- Java (JDK 8+)
- Java Swing / AWT
- JDBC
- MySQL 8.0
- MySQL Connector/J 8.0.33

---

## 📂 Project Structure


SudokuValidator-Java/
│
├── sudokuvalidator/
│ ├── Main.java
│ ├── MainWindow.java
│ ├── GamePanel.java
│ ├── SudokuGrid.java
│ ├── SudokuEngine.java
│ ├── ValidatorPanel.java
│ ├── LeaderboardPanel.java
│ ├── HistoryPanel.java
│ ├── DatabaseConnection.java
│
├── database.sql
├── README.md


---

## ⚙️ Setup Instructions

### 1️⃣ Clone Repo
```bash
git clone https://github.com/adarsh-kumar-lab/Sudoku-Java.git
cd SudokuValidator-Java
2️⃣ Setup MySQL

Open MySQL and run:

SOURCE database.sql;
3️⃣ Update DB Password

In DatabaseConnection.java:

private static final String PASSWORD = "your_password";
4️⃣ Add MySQL Connector

Download:
👉 https://dev.mysql.com/downloads/connector/j/

Add JAR to classpath:

javac -cp ".;mysql-connector-j-8.0.33.jar" sudokuvalidator/*.java
java -cp ".;mysql-connector-j-8.0.33.jar" sudokuvalidator.Main
❗ Common Errors & Fixes
❌ 1. MySQL JDBC Driver not found
Error: MySQL JDBC Driver not found

✅ Fix:

Download connector JAR
Add to classpath (VERY IMPORTANT)
❌ 2. Cannot find symbol getBoard()
error: cannot find symbol getBoard()

✅ Fix:
Add this in SudokuGrid.java:

public int[][] getBoard() {
    return SudokuEngine.copyBoard(board);
}
❌ 3. Cannot find symbol clearInvalid()
error: cannot find symbol clearInvalid()

✅ Fix:

public void clearInvalid() {
    invalid = new boolean[9][9];
    refreshAll();
}
❌ 4. ClassNotFoundException: sudokuvalidator.Main
Could not find or load main class

✅ Fix:

Run from correct folder
Ensure package structure is correct
❌ 5. Database connection failed

✅ Check:

MySQL running
DB name = sudoku_db
Password correct
🧪 Testing

✔️ 32 Test Cases Passed
✔️ UI Responsive (SwingWorker used)
✔️ DB Integration Verified

🎯 Conclusion

This project demonstrates:

OOP Design
GUI Development
Algorithm Implementation
Database Integration
Real-world Software Development
🔮 Future Improvements
Multiplayer Mode 🌐
Android App 📱
OCR Sudoku Scanner 📷
Web Version 🌍
📚 References
Java Docs
MySQL Docs
GeeksforGeeks Sudoku
LeetCode Problems 36 & 37
⭐ If you like this project

Give it a ⭐ on GitHub!


---

# 🔥 IMPORTANT (YOUR CURRENT ERRORS FIXED)

From your screenshot, you have **3 main problems**:

---

## 🧨 1. MySQL Driver NOT FOUND

💀 Error:

MySQL JDBC Driver not found


💡 Solution:

Step-by-step:

1. Download:
👉 mysql-connector-j-8.0.33.jar

2. Put in project folder

3. Run:

```bash
javac -cp ".;mysql-connector-j-8.0.33.jar" sudokuvalidator/*.java
java -cp ".;mysql-connector-j-8.0.33.jar" sudokuvalidator.Main
🧨 2. getBoard() missing

Add inside SudokuGrid.java:

public int[][] getBoard() {
    return SudokuEngine.copyBoard(board);
}
🧨 3. clearInvalid() missing

Add:

public void clearInvalid() {
    invalid = new boolean[9][9];
    refreshAll();
}
🧨 4. Main class not found

Run like this:

cd files   ← (your root folder)
javac sudokuvalidator/*.java
java sudokuvalidator.Main
🚀 FINAL GITHUB UPLOAD STEPS
git init
git add .
git commit -m "Initial commit - Sudoku Project"
git branch -M main
git remote add origin https://github.com/your-username/SudokuValidator-Java.git
git push -u origin main
🎯 Pro Tip (Very Important)

Your folder MUST look like this:

SudokuValidator-Java/
   └── sudokuvalidator/

NOT like:

SudokuValidator-Java/
   Main.java ❌
