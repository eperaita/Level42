import SwiftUI
import shared

struct ProjectsView: View {
    @EnvironmentObject private var viewModel: ProfileViewModel
    @Binding var navigationPath: NavigationPath
    
    private let customYellow = Color(red: 1.0, green: 0.988, blue: 0.0)
    private let customBlack = Color.black

    var body: some View {
        ZStack {
            // Fondo amarillo
            customYellow.ignoresSafeArea()
            
            // Contenido principal
            Group {
                if case .loading = viewModel.projectsState {
                    LoadingView(navigationPath: $navigationPath)
                } else if case .error(let message) = viewModel.projectsState {
                    VStack {
                        Text("Error: \(message)")
                            .foregroundColor(.red)
                        Button("Retry") {
                            loadProjects()
                        }
                        .padding()
                        .background(customBlack)
                        .foregroundColor(customYellow)
                        .cornerRadius(8)
                    }
                } else if SessionManager.shared.selectedUserProfile?.projects.isEmpty ?? true {
                    Text("No projects found")
                        .foregroundColor(customBlack)
                } else {
                    VerticalCarouselView(
                        projects: SessionManager.shared.selectedUserProfile?.projects ?? [],
                        navigationPath: $navigationPath
                    )
                }
            }

            // Botón Atrás
            Button(action: {
                navigationPath.removeLast()
            }) {
                Image(systemName: "chevron.left")
                    .font(.system(size: 24, weight: .bold))
                    .foregroundColor(customYellow)
                    .frame(width: 50, height: 50)
                    .background(customBlack)
                    .clipShape(Circle())
            }
            .padding(.leading, 16)
            .padding(.top, 16)
            .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .topLeading)
        }
        .navigationBarHidden(true)
        .onAppear {
            loadProjects()
        }
    }

    private func loadProjects() {
        guard let login = SessionManager.shared.selectedUserProfile?.login else { return }
        viewModel.loadProjectsForUser(login: login)
    }
}
