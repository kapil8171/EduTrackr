# 📖 EduTrackr – A Smart Study Assistant App

![Made with Kotlin](https://img.shields.io/badge/Made%20with-Kotlin-7f52ff.svg)
![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-blue)
![Material3](https://img.shields.io/badge/Material%20Design-3-4285F4.svg)
![Architecture-MVI](https://img.shields.io/badge/Architecture-MVI-green)
![Status](https://img.shields.io/badge/Status-Active-brightgreen)

**EduTrackr** is a modern, responsive study assistant app for students.  
It helps track **subjects, tasks, and study sessions** with **real-time progress visualization**, enabling students to manage their study schedules smartly and effectively.  

---

## ✨ Features

- 📚 **Subjects Management** – Add subjects with goal study hours.  
- 📝 **Task Management** – Create tasks with:
  - Title  
  - Description  
  - Due Date  
  - Priority Level (Low / Medium / High)  
- 🎯 **Study Sessions** – Start/stop sessions with a timer and track study hours per subject.  
- ✅ **Progress Tracking** – Visualize subject progress based on completed study hours vs. goal.  
- 📅 **Upcoming & Completed Tasks** – Mark tasks as completed and view history.  
- 🔄 **Update/Delete** – Subjects, tasks, and study sessions can be updated or deleted anytime.  
- 📊 **Dashboard Overview** –  
  - Total subjects  
  - Goal vs. studied hours  
  - Upcoming tasks  
  - Completed tasks  
  - Recent study sessions  
- 🔔 **Smart Notifications** – Foreground service with session timer notifications + deep linking.  

---

## 🛠️ Tech Stack

| Layer            | Technology/Library                                |
|------------------|---------------------------------------------------|
| Language         | Kotlin                                            |
| UI Design        | Jetpack Compose + Material3                       |
| Architecture     | Clean Architecture + MVI Pattern                  |
| Asynchronous     | Kotlin Coroutines + Flow                          |
| Local Storage    | Room Database                                     |
| Dependency Inj.  | Dagger Hilt (`@HiltViewModel`)                    |
| Navigation       | Jetpack Compose Navigation + Deep Linking         |
| Services         | Bound & Foreground Service for timer tracking     |
| Notifications    | Post Notifications with deep link to app session  |

---

## 📲 App Flow

```text
DashboardScreen
    ↓
SubjectScreen (subject details + tasks + progress)
    ├──→ TaskScreen (Add/Edit tasks)
    └──→ SessionScreen (Start/Stop study session)
```

---


## 📸 Screenshots

<p align="center">
  <div>
    <p align="center"><b>📊 Dashboard Screen</b></p>
    <img src="https://github.com/user-attachments/assets/4d0bf410-f8e4-4a0e-89bb-4d9a8a686fb8" alt="Dashboard" width="160"/>
  </div>
  <div>
    <p align="center"><b>📚 Subject Screen</b></p>
    <img src="https://github.com/user-attachments/assets/46cc083b-a1fb-487a-a930-2f97d5d382da" alt="Subject" width="160"/>
  </div>
</p>

<p align="center">
  <div>
    <p align="center"><b>📝 Task Screen</b></p>
    <img src="https://github.com/user-attachments/assets/44e2432d-fda5-4bad-ab42-c01fff992870" alt="Task" width="160"/>
  </div>
  <div>
    <p align="center"><b>⏱️ Timer Screen</b></p>
    <img src="https://github.com/user-attachments/assets/b83fe653-6e60-49c5-8852-d1deae841d08" alt="Timer" width="160"/>
  </div>
</p>





