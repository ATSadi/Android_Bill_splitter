package com.example.roomshare;

import android.content.Context;

import com.example.roomshare.data.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DatabaseHelper {
    private static volatile DatabaseHelper INSTANCE;
    private final AppDatabase database;
    private final RoomDao roomDao;
    private final RoommateDao roommateDao;
    private final BillDao billDao;
    private final ExpenseDao expenseDao;
    private final ChoreDao choreDao;
    private Long currentRoomId;
    private final ExecutorService executorService;

    private DatabaseHelper(Context context) {
        database = AppDatabase.getDatabase(context);
        roomDao = database.roomDao();
        roommateDao = database.roommateDao();
        billDao = database.billDao();
        expenseDao = database.expenseDao();
        choreDao = database.choreDao();
        executorService = Executors.newSingleThreadExecutor();
    }

    public static DatabaseHelper getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (DatabaseHelper.class) {
                if (INSTANCE == null) {
                    INSTANCE = new DatabaseHelper(context.getApplicationContext());
                }
            }
        }
        return INSTANCE;
    }

    public void getAllRooms(Callback<List<RoomEntity>> callback) {
        executorService.execute(() -> {
            try {
                List<RoomEntity> result = roomDao.getAllRooms();
                callback.onSuccess(result);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }

    public void createOrGetRoom(String name, Callback<Long> callback) {
        executorService.execute(() -> {
            try {
                RoomEntity existing = roomDao.getRoomByName(name);
                long result = (existing != null) ? existing.id : roomDao.insertRoom(new RoomEntity(name));
                callback.onSuccess(result);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }

    public void setCurrentRoom(long roomId) {
        currentRoomId = roomId;
    }

    public Long getCurrentRoomId() {
        return currentRoomId;
    }

    public void addRoommate(String name, String email, String phone, long roomId, Callback<Long> callback) {
        executorService.execute(() -> {
            try {
                List<Roommate> existing = roommateDao.getRoommateByName(name);
                for (Roommate r : existing) {
                    if (r.roomId != roomId) {
                        callback.onError(new Exception("Person cannot be in multiple rooms"));
                        return;
                    }
                }
                Roommate roommate = new Roommate(name, email, phone, roomId);
                long result = roommateDao.insertRoommate(roommate);
                callback.onSuccess(result);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }

    public void getRoommates(long roomId, Callback<List<Roommate>> callback) {
        executorService.execute(() -> {
            try {
                List<Roommate> result = roommateDao.getRoommatesByRoom(roomId);
                callback.onSuccess(result);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }

    public void addBill(String name, double amount, long roomId, Callback<Long> callback) {
        executorService.execute(() -> {
            try {
                Bill bill = new Bill(name, amount, roomId);
                long result = billDao.insertBill(bill);
                callback.onSuccess(result);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }

    public void getBills(long roomId, Callback<List<Bill>> callback) {
        executorService.execute(() -> {
            try {
                List<Bill> result = billDao.getBillsByRoom(roomId);
                callback.onSuccess(result);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }

    public void addExpense(String name, double amount, long payerId, long roomId, int splitCount, Callback<Long> callback) {
        executorService.execute(() -> {
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                String date = dateFormat.format(new Date());
                Expense expense = new Expense(name, amount, payerId, roomId, splitCount, date);
                long result = expenseDao.insertExpense(expense);
                callback.onSuccess(result);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }

    public void getExpenses(long roomId, Callback<List<Expense>> callback) {
        executorService.execute(() -> {
            try {
                List<Expense> result = expenseDao.getExpensesByRoom(roomId);
                callback.onSuccess(result);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }

    public void addChore(String name, Long assignedToId, long roomId, Callback<Long> callback) {
        executorService.execute(() -> {
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                String date = dateFormat.format(new Date());
                Chore chore = new Chore(name, assignedToId, roomId, 0, date);
                long result = choreDao.insertChore(chore);
                callback.onSuccess(result);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }

    public void getChores(long roomId, Callback<List<Chore>> callback) {
        executorService.execute(() -> {
            try {
                List<Chore> result = choreDao.getChoresByRoom(roomId);
                callback.onSuccess(result);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }

    public void markChoreComplete(long choreId, long roomId, Callback<Void> callback) {
        executorService.execute(() -> {
            try {
                Chore chore = choreDao.getChoreById(choreId, roomId);
                if (chore != null) {
                    chore.completed = 1;
                    choreDao.updateChore(chore);
                }
                callback.onSuccess(null);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }

    public void calculateBalance(long roomId, Callback<BalanceResult> callback) {
        executorService.execute(() -> {
            try {
                List<Roommate> roommates = roommateDao.getRoommatesByRoom(roomId);
                List<Expense> expenses = expenseDao.getExpensesByRoom(roomId);

                Map<Long, Double> paidMap = new HashMap<>();
                Map<Long, Double> owedMap = new HashMap<>();

                for (Roommate roommate : roommates) {
                    paidMap.put(roommate.id, 0.0);
                    owedMap.put(roommate.id, 0.0);
                }

                for (Expense expense : expenses) {
                    double perPerson = expense.amount / expense.splitCount;
                    paidMap.put(expense.payerId, paidMap.get(expense.payerId) + expense.amount);
                    for (Roommate roommate : roommates) {
                        owedMap.put(roommate.id, owedMap.get(roommate.id) + perPerson);
                    }
                }

                List<RoommateBalance> balances = new ArrayList<>();
                for (Roommate roommate : roommates) {
                    double paid = paidMap.get(roommate.id);
                    double owed = owedMap.get(roommate.id);
                    double net = paid - owed;
                    balances.add(new RoommateBalance(roommate, paid, owed, net));
                }

                callback.onSuccess(new BalanceResult(balances));
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }

    public void getHistory(long roomId, String filterText, Callback<List<HistoryItem>> callback) {
        executorService.execute(() -> {
            try {
                List<Chore> chores = choreDao.getChoresByRoom(roomId);
                List<Expense> expenses = expenseDao.getExpensesByRoom(roomId);
                List<Roommate> roommates = roommateDao.getRoommatesByRoom(roomId);

                Map<Long, String> roommateMap = new HashMap<>();
                for (Roommate roommate : roommates) {
                    roommateMap.put(roommate.id, roommate.name);
                }

                List<HistoryItem> items = new ArrayList<>();

                for (Chore chore : chores) {
                    String assignedTo = (chore.assignedToId != null && roommateMap.containsKey(chore.assignedToId))
                            ? roommateMap.get(chore.assignedToId) : "Unassigned";
                    items.add(new HistoryItem.ChoreItem(chore, assignedTo));
                }

                for (Expense expense : expenses) {
                    String payer = roommateMap.getOrDefault(expense.payerId, "Unknown");
                    items.add(new HistoryItem.ExpenseItem(expense, payer));
                }

                // Filter if needed
                if (filterText != null && !filterText.trim().isEmpty()) {
                    String filter = filterText.toLowerCase();
                    List<HistoryItem> filtered = new ArrayList<>();
                    for (HistoryItem item : items) {
                        if (item instanceof HistoryItem.ChoreItem) {
                            HistoryItem.ChoreItem choreItem = (HistoryItem.ChoreItem) item;
                            if (choreItem.chore.name.toLowerCase().contains(filter) ||
                                choreItem.assignedTo.toLowerCase().contains(filter)) {
                                filtered.add(item);
                            }
                        } else if (item instanceof HistoryItem.ExpenseItem) {
                            HistoryItem.ExpenseItem expenseItem = (HistoryItem.ExpenseItem) item;
                            if (expenseItem.expense.name.toLowerCase().contains(filter) ||
                                expenseItem.payer.toLowerCase().contains(filter)) {
                                filtered.add(item);
                            }
                        }
                    }
                    items = filtered;
                }

                callback.onSuccess(items);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }

    public void generateReport(long roomId, Callback<Report> callback) {
        executorService.execute(() -> {
            try {
                List<Chore> chores = choreDao.getChoresByRoom(roomId);
                List<Expense> expenses = expenseDao.getExpensesByRoom(roomId);
                List<Roommate> roommates = roommateDao.getRoommatesByRoom(roomId);

                int totalChores = chores.size();
                int completedChores = 0;
                for (Chore chore : chores) {
                    if (chore.completed == 1) completedChores++;
                }
                int pendingChores = totalChores - completedChores;

                int totalExpenses = expenses.size();
                double totalAmount = 0;
                for (Expense expense : expenses) {
                    totalAmount += expense.amount;
                }
                double avgPerExpense = totalExpenses > 0 ? totalAmount / totalExpenses : 0.0;

                List<Pair<String, Integer>> choresPerRoommate = new ArrayList<>();
                for (Roommate roommate : roommates) {
                    int count = 0;
                    for (Chore chore : chores) {
                        if (chore.assignedToId != null && chore.assignedToId == roommate.id && chore.completed == 1) {
                            count++;
                        }
                    }
                    choresPerRoommate.add(new Pair<>(roommate.name, count));
                }

                Report report = new Report(totalChores, completedChores, pendingChores,
                        totalExpenses, totalAmount, avgPerExpense, choresPerRoommate);
                callback.onSuccess(report);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }

    public void deleteRoom(long roomId, Callback<Void> callback) {
        executorService.execute(() -> {
            try {
                RoomEntity room = roomDao.getRoomById(roomId);
                if (room != null) {
                    roomDao.deleteRoom(room);
                    if (currentRoomId != null && currentRoomId == roomId) {
                        currentRoomId = null;
                    }
                }
                callback.onSuccess(null);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }

    public interface Callback<T> {
        void onSuccess(T result);
        void onError(Exception e);
    }

    public static class BalanceResult {
        public List<RoommateBalance> balances;

        public BalanceResult(List<RoommateBalance> balances) {
            this.balances = balances;
        }
    }

    public static class RoommateBalance {
        public Roommate roommate;
        public double paid;
        public double owed;
        public double net;

        public RoommateBalance(Roommate roommate, double paid, double owed, double net) {
            this.roommate = roommate;
            this.paid = paid;
            this.owed = owed;
            this.net = net;
        }
    }

    public static abstract class HistoryItem {
        public static class ChoreItem extends HistoryItem {
            public Chore chore;
            public String assignedTo;

            public ChoreItem(Chore chore, String assignedTo) {
                this.chore = chore;
                this.assignedTo = assignedTo;
            }
        }

        public static class ExpenseItem extends HistoryItem {
            public Expense expense;
            public String payer;

            public ExpenseItem(Expense expense, String payer) {
                this.expense = expense;
                this.payer = payer;
            }
        }
    }

    public static class Report {
        public int totalChores;
        public int completedChores;
        public int pendingChores;
        public int totalExpenses;
        public double totalAmount;
        public double avgPerExpense;
        public List<Pair<String, Integer>> choresPerRoommate;

        public Report(int totalChores, int completedChores, int pendingChores,
                      int totalExpenses, double totalAmount, double avgPerExpense,
                      List<Pair<String, Integer>> choresPerRoommate) {
            this.totalChores = totalChores;
            this.completedChores = completedChores;
            this.pendingChores = pendingChores;
            this.totalExpenses = totalExpenses;
            this.totalAmount = totalAmount;
            this.avgPerExpense = avgPerExpense;
            this.choresPerRoommate = choresPerRoommate;
        }
    }

    public static class Pair<F, S> {
        public F first;
        public S second;

        public Pair(F first, S second) {
            this.first = first;
            this.second = second;
        }
    }
}

