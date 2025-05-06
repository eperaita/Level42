import SwiftUI
import shared

class ProfileViewModel: ObservableObject {

    // Estados
    @Published var profileLoaded: Bool = false
    @Published var projectsLoaded: Bool = false
    @Published var projects: [Project] = []
    @Published var userProfile: UserProfile?
    @Published var authError: String? = nil

    // Función para manejar el callback de OAuth
    func handleAuthCallback(code: String) {
            DispatchQueue.global().async {
                do {
                    try Api42().handleCallbackWrapper(code: code)
                    DispatchQueue.main.async {
                        self.profileLoaded = true
                        self.authError = nil
                    }
                } catch {
                    print("Error handling auth callback: \(error)")
                    DispatchQueue.main.async {
                        self.profileLoaded = false
                        self.authError = error.localizedDescription
                        // Limpiar el error después de 3 segundos
                        DispatchQueue.main.asyncAfter(deadline: .now() + 3) {
                           self.authError = nil
                        }

                    }
                }
            }
        }

    func loadProjects() {

        // si ya hay proyectos cargados (sincrónico)
        if let cachedProjects = SessionManager.shared.userProfile?.projects,
           !cachedProjects.isEmpty {
            DispatchQueue.main.async {
                self.projects = cachedProjects
                self.projectsLoaded = true
            }
            return
        }
        // 2. Si no, llama a la API en background
        DispatchQueue.main.async { //  Asegura ejecución en hilo principal
            Task {
                do {
                    try await Api42().getProjectsWrapper()

                    if let userProfile = SessionManager.shared.userProfile {
                        self.projects = userProfile.projects
                        self.projectsLoaded = true
                    }
                } catch {
                    print("Error al cargar proyectos: \(error.localizedDescription)")

                    // Intentar refrescar el token si el error es 401
                    if error.localizedDescription.contains("401") {
                        do {
                            let refreshSuccess = try await Api42().refreshTokenWrapper()
                            if refreshSuccess.boolValue {
                                // Reintentar después de refrescar
                                try await Api42().getProjectsWrapper()
                                if let userProfile = SessionManager.shared.userProfile {
                                    self.projects = userProfile.projects
                                    self.projectsLoaded = true
                                    return
                                }
                            }
                        } catch {
                            print("Error al refrescar token: \(error.localizedDescription)")
                        }
                    }

                    self.projectsLoaded = false
                    self.projects = []
                }
            }
        }
    }
}



