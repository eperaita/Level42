import SwiftUI
import shared

class ProfileViewModel: ObservableObject {
    
    // Estados
    @Published var authState: AuthState = .idle
    @Published var searchState: SearchState = .idle
    @Published var projectsState: ProjectsState = .idle
    
    enum AuthState : Equatable {
        case idle
        case loading
        case success(authData: AuthData)
        case error(message: String)
    }
    
    enum SearchState: Equatable  {
        case idle
        case loading
        case success(user: SelectedUserProfile)
        case error(message: String)
    }
    
    enum ProjectsState: Equatable  {
        case idle
        case loading
        case success(projects: [Project])
        case error(message: String)
    }

    // Función para manejar el callback de OAuth
    func handleAuthCallback(code: String) {
        DispatchQueue.global().async {
            do {
                print("[VIEWMODEL] Code: \(code)")
                self.updateAuthState(.loading)
                
                let authData = try Api42().handleCallbackWrapper(code: code)
                
                DispatchQueue.main.async {
                    // Acceso CORRECTO a las propiedades (snake_case igual que en Kotlin)
                    SessionManager.shared.access_token = authData.access_token
                    SessionManager.shared.refresh_token = authData.refresh_token
                    SessionManager.shared.user_id = authData.userId as? KotlinInt // userId es Int en Kotlin
                    SessionManager.shared.user_login = authData.userLogin
                    SessionManager.shared.user_image_url = authData.imageUrl
                                    
                    self.updateAuthState(.success(authData: authData))
                                    
                    let userId = SessionManager.shared.user_id?.int32Value ?? -1
                    print("[VIEWMODEL]: Logged as: \(SessionManager.shared.user_login ?? ""), id: \(userId)")
                }
                
            } catch {
                print("[VIEWMODEL] Auth error: \(error)")
                self.updateAuthState(.error(message: error.localizedDescription))
                SessionManager.shared.clearSession()
            }
        }
    }
        
    func searchForUser(login: String) {
        DispatchQueue.global().async {
            do {
                self.updateSearchState(.loading)
                print("[VIEWMODEL] Searching for login: \(login)")
                
                let user = try Api42().searchUserWrapper(login: login)
                SessionManager.shared.selectedUserProfile = user
                
                DispatchQueue.main.async {
                    self.updateSearchState(.success(user: user))
                    print("[VIEWMODEL] User found: \(user.login)")
                }
            } catch let error as Api42.UserNotFoundException {
                self.updateSearchState(.error(message: "Usuario no encontrado"))
            } catch {
                self.updateSearchState(.error(message: error.localizedDescription))
            }
        }
    }

    func loadProjectsForUser(login: String) {
        DispatchQueue.global().async {
            do {
                self.updateProjectsState(.loading)
                print("[VIEWMODEL] Loading projects for: \(login)")
                
                let projects = try Api42().getProjectsWrapper(login: login)
                
                DispatchQueue.main.async {
                    if var selectedUser = SessionManager.shared.selectedUserProfile {
                        selectedUser.projects = projects
                        SessionManager.shared.selectedUserProfile = selectedUser
                    }
                    self.updateProjectsState(.success(projects: projects))
                    print("[VIEWMODEL] Projects loaded: \(projects.count)")
                }
            } catch {
                print("[VIEWMODEL] Projects error: \(error)")
                self.updateProjectsState(.error(message: error.localizedDescription))
                
                if error.localizedDescription.contains("401") {
                    self.refreshTokenAndRetry(login: login)
                }
            }
        }
    }
        
    private func refreshTokenAndRetry(login: String) {
        Task {
            do {
                let success = try await Api42().refreshTokenWrapper()
                if success as! Bool {
                    DispatchQueue.main.async {
                        self.loadProjectsForUser(login: login)
                    }
                }
            } catch {
                print("[VIEWMODEL] Token refresh failed: \(error)")
            }
        }
    }
    
    // MARK: - State Management
        
    private func updateAuthState(_ state: AuthState) {
        DispatchQueue.main.async {
            self.authState = state
        }
    }
    
    private func updateSearchState(_ state: SearchState) {
        DispatchQueue.main.async {
            self.searchState = state
        }
    }
    
    private func updateProjectsState(_ state: ProjectsState) {
        DispatchQueue.main.async {
            self.projectsState = state
        }
    }
    
    func resetState() {
        DispatchQueue.main.async {
            self.authState = .idle
            self.searchState = .idle
            self.projectsState = .idle
        }
    }
    
    func clearSearch() {
        DispatchQueue.main.async {
            SessionManager.shared.selectedUserProfile = nil
            self.searchState = .idle
        }
    }
    
    func clearAuthError() {
        DispatchQueue.main.async {
            if case .error = self.authState {
                self.authState = .idle
            }
        }
    }
}
