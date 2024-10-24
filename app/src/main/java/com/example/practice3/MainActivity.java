package com.example.practice3;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private SharedPreferences themeSettings;
    private SharedPreferences.Editor settingsEditor;
    private ImageButton imageTheme;
    private boolean playerXTurn = true; // Игрок всегда играет за X
    private boolean gameOver = false;   // Флаг окончания игры
    private Random random = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Настройки темы
        themeSettings = getSharedPreferences("SETTINGS", MODE_PRIVATE);
        if (!themeSettings.contains("MODE_NIGHT_ON")) {
            settingsEditor = themeSettings.edit();
            settingsEditor.putBoolean("MODE_NIGHT_ON", false);
            settingsEditor.apply();
        }
        setCurrentTheme();

        setContentView(R.layout.activity_main);

        imageTheme = findViewById(R.id.imagebtn);
        updateImageButton();
        imageTheme.setOnClickListener(v -> toggleTheme());

        initializeGameGrid();

        Button restartBtn = findViewById(R.id.restartBtn);
        restartBtn.setOnClickListener(v -> resetGame());

        loadStats(); // Загрузка статистики при старте
    }

    // Логика смены темы
    private void toggleTheme() {
        boolean nightMode = themeSettings.getBoolean("MODE_NIGHT_ON", false);
        AppCompatDelegate.setDefaultNightMode(nightMode ? AppCompatDelegate.MODE_NIGHT_NO : AppCompatDelegate.MODE_NIGHT_YES);
        settingsEditor = themeSettings.edit();
        settingsEditor.putBoolean("MODE_NIGHT_ON", !nightMode);
        settingsEditor.apply();
        Toast.makeText(MainActivity.this, nightMode ? "Тёмная тема отключена" : "Тёмная тема включена", Toast.LENGTH_SHORT).show();
        updateImageButton();
    }

    // Обновление иконки кнопки смены темы
    private void updateImageButton() {
        imageTheme.setImageResource(themeSettings.getBoolean("MODE_NIGHT_ON", false) ? R.drawable.sun : R.drawable.moon);
    }

    // Устанавливаем текущую тему при запуске приложения
    private void setCurrentTheme() {
        AppCompatDelegate.setDefaultNightMode(themeSettings.getBoolean("MODE_NIGHT_ON", false) ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
    }

    // Инициализация игрового поля
    private void initializeGameGrid() {
        GridLayout gridLayout = findViewById(R.id.gameGrid);
        for (int i = 0; i < gridLayout.getChildCount(); i++) {
            Button button = (Button) gridLayout.getChildAt(i);
            int finalI = i;
            button.setOnClickListener(v -> makeMove(button, finalI));
        }
    }

    // Ход игрока
    private void makeMove(Button button, int index) {
        if (!button.getText().toString().isEmpty() || gameOver) return;

        button.setText(playerXTurn ? "X" : "O");
        if (checkGameState()) return;

        playerXTurn = !playerXTurn;
        if (!gameOver && !playerXTurn) botMakeMove();
    }

    // Ход бота
    private void botMakeMove() {
        List<Button> availableButtons = getAvailableButtons();
        if (!availableButtons.isEmpty()) {
            availableButtons.get(random.nextInt(availableButtons.size())).setText("O");
            checkGameState();
            playerXTurn = true;
        }
    }

    // Получение списка доступных для хода кнопок
    private List<Button> getAvailableButtons() {
        GridLayout gridLayout = findViewById(R.id.gameGrid);
        List<Button> availableButtons = new ArrayList<>();
        for (int i = 0; i < gridLayout.getChildCount(); i++) {
            Button button = (Button) gridLayout.getChildAt(i);
            if (button.getText().toString().isEmpty()) availableButtons.add(button);
        }
        return availableButtons;
    }

    // Проверка состояния игры
    private boolean checkGameState() {
        String winner = checkForWinner();
        if (winner != null) {
            endGame(winner.equals("X") ? "Крестики" : "Нолики");
            saveStats(winner);
            return true;
        } else if (checkForDraw()) {
            endGame("Ничья");
            saveStats("draw");
            return true;
        }
        return false;
    }

    // Проверка победителя
    private String checkForWinner() {
        String[][] board = getBoard();
        for (int i = 0; i < 3; i++) {
            if (checkLine(board[i][0], board[i][1], board[i][2])) return board[i][0];
            if (checkLine(board[0][i], board[1][i], board[2][i])) return board[0][i];
        }
        if (checkLine(board[0][0], board[1][1], board[2][2]) || checkLine(board[0][2], board[1][1], board[2][0])) return board[1][1];
        return null;
    }

    // Проверка, что все элементы линии равны и не пусты
    private boolean checkLine(String a, String b, String c) {
        return a.equals(b) && b.equals(c) && !a.isEmpty();
    }

    // Получение текущего состояния игрового поля
    private String[][] getBoard() {
        GridLayout gridLayout = findViewById(R.id.gameGrid);
        String[][] board = new String[3][3];
        for (int i = 0; i < gridLayout.getChildCount(); i++) {
            board[i / 3][i % 3] = ((Button) gridLayout.getChildAt(i)).getText().toString();
        }
        return board;
    }

    // Проверка ничьей
    private boolean checkForDraw() {
        return getAvailableButtons().isEmpty();
    }

    // Завершение игры
    private void endGame(String result) {
        Toast.makeText(this, "Результат: " + result, Toast.LENGTH_SHORT).show();
        gameOver = true;
    }

    // Сброс игры
    private void resetGame() {
        GridLayout gridLayout = findViewById(R.id.gameGrid);
        for (int i = 0; i < gridLayout.getChildCount(); i++) {
            Button button = (Button) gridLayout.getChildAt(i);
            button.setText("");
        }
        gameOver = false;
        playerXTurn = true;
    }

    // Загрузка статистики
    private void loadStats() {
        SharedPreferences prefs = getSharedPreferences("TicTacToeStats", MODE_PRIVATE);
        int xWins = prefs.getInt("xWins", 0);
        int oWins = prefs.getInt("oWins", 0);
        int draws = prefs.getInt("draws", 0);
        ((TextView) findViewById(R.id.statsView)).setText("Крестики: " + xWins + " | Нолики: " + oWins + " | Ничья: " + draws);
    }

    // Сохранение статистики
    private void saveStats(String winner) {
        SharedPreferences prefs = getSharedPreferences("TicTacToeStats", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        if (winner.equals("X")) editor.putInt("xWins", prefs.getInt("xWins", 0) + 1);
        else if (winner.equals("O")) editor.putInt("oWins", prefs.getInt("oWins", 0) + 1);
        else editor.putInt("draws", prefs.getInt("draws", 0) + 1);

        editor.apply();
        loadStats();
    }
}
