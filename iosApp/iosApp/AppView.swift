import SwiftUI
import shared

struct ProjectID: Hashable, Codable {
    let value: Int
}

struct AppView: View {

    // 1. Crear NavigationPath // Pila de navegación en SwiftUI que almacena el estado de navegacion y e historial
    @State private var navigationPath = NavigationPath()

    // 2. Obtener el ViewModel: (se inyecta en el entorno usando @EnvironmentObject)
    @StateObject var viewModel = ProfileViewModel()

    // 3. Las variables a observar no hace falta declararlas.
    // Con @Published  SwiftUI actualiza automáticamente las vistas que dependen de él.

    var body: some View {

        //4. COnfigurar NavigationStack (Como NavHost)

        //Cada vista recibe navigationPath como @Binding, lo que le permite navegar a otra pantalla o retroceder
        NavigationStack(path: $navigationPath) { LoginView(navigationPath: $navigationPath)
            .navigationDestination(for: String.self) { route in
                switch route {
                case "loading":
                    LoadingView(navigationPath: $navigationPath)
                case "welcome":
                    WelcomeView(navigationPath: $navigationPath)
                case "profile":
                    ProfileView(navigationPath: $navigationPath)
                case "projects":
                    ProjectsView(navigationPath: $navigationPath)
                case "skills":
                    SkillsView(navigationPath: $navigationPath)
                case "user_not_found":
                    UserNotFoundView(navigationPath: $navigationPath)
                default:
                    LoginView(navigationPath: $navigationPath)
                }
            }
            .navigationDestination(for: ProjectID.self) { id in
                SelectedProjectView(projectId: id.value, navigationPath: $navigationPath)
            }
            
        }
        //Esto es para recoger el callback  - en Android lo recoge Mainactivity
        .onOpenURL { url in handleIncomingURL(url) }
        
        //5. Navegar entre pantallas
        .onChange(of: viewModel.authState) { newState in
            handleAuthStateChange(newState)
        }
        .onChange(of: viewModel.searchState) { newState in
            handleSearchStateChange(newState)
        }
        //Pop up error en inicio sesion
        .alert("Error",isPresented: .constant(viewModel.authState.isError),
           actions: { Button("OK") { viewModel.clearAuthError() } },
           message: {
               if case let .error(message) = viewModel.authState {
                   Text(message)
               }
           })
    }
    
    // Función auxiliar para encontrar el proyecto
    private func findProjectById(_ id: Int) -> Project? {
        return SessionManager.shared.selectedUserProfile?.projects.first { $0.project.id == id }
    }

    func handleIncomingURL(_ url: URL) {
        guard let components = URLComponents(url: url, resolvingAgainstBaseURL: true),
              let code = components.queryItems?.first(where: { $0.name == "code" })?.value else {
            
            viewModel.authState = .error(message: "URL de callback inválida")
            return
        }
        viewModel.handleAuthCallback(code: code)
    }
    
    // --- Funciones manejadoras  ---
    private func handleAuthStateChange(_ state: ProfileViewModel.AuthState) {
        DispatchQueue.main.async {
            switch state {
            case .success:
                navigationPath.removeLast(navigationPath.count)
                navigationPath.append("welcome")
            case .error(let message):
                print("Auth error: \(message)")
                navigationPath.removeLast(navigationPath.count)
                navigationPath.append("login")
            default:
                break
            }
        }
    }
    
    private func handleSearchStateChange(_ state: ProfileViewModel.SearchState) {
        DispatchQueue.main.async {
            switch state {
            case .success:
                navigationPath.append("profile")
            case .error:
                navigationPath.append("user_not_found")
            default:
                break
            }
        }
    }
}


struct AppView_Previews: PreviewProvider {  // <<- ¡Nombre cambiado a AppView_Previews!
    static var previews: some View {
        AppView()
    }
}


// Extensión para verificar errores
extension ProfileViewModel.AuthState {
    var isError: Bool {
        if case .error = self { return true }
        return false
    }
}
