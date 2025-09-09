# EduTrackr 📚

**EduTrackr – A smart study assistant app** that helps students track subjects, tasks, and study hours with real-time progress visualization.

---

## 🚀 Features

- Add subjects with goal study hours.
- Create and manage tasks with:
  - Title  
  - Description  
  - Due Date  
  - Priority Level (Low / Medium / High)
- Start study sessions for each subject and track progress in real-time.
- Mark tasks as completed using a checkbox (moves to "Completed Tasks").
- View upcoming and completed tasks in one place.
- Track sessions within each subject (start, stop, finish, delete).
- Update or delete subjects, tasks, and sessions anytime.
- Dashboard overview with:
  - Total subjects  
  - Goal study hours vs. studied hours  
  - Upcoming tasks  
  - Completed tasks  
  - Recent study sessions

---

## 📱 Screens

1. **Dashboard Screen** – Overview of subjects, study progress, and tasks.  
2. **Subject Screen** – Subject details, progress tracking, upcoming tasks, and add tasks option.  
3. **Task Screen** – Add/manage tasks with details.  
4. **Session Screen** – Start, pause, stop, and save study sessions with timer and notifications.

---

## 🛠️ Tech Stack

- **Language:** Kotlin  
- **UI:** Jetpack Compose + Material 3  
- **Architecture:** Clean Architecture (MVI Pattern)  
- **Database:** Room (Local Storage)  
- **Async Programming:** Coroutines & Flows  
- **Dependency Injection:** Dagger Hilt (`@HiltViewModel`)  
- **Service & Notifications:** Bound & Foreground Service for study timer, Post Notifications, Deep Linking from notification  
- **Navigation:** Compose Navigation for smooth navigation  

---

## 📸 Screenshots

(Add app screenshots here)

---

## 📦 Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/your-username/EduTrackr.git
