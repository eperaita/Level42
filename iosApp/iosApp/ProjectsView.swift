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
            if !viewModel.projectsLoaded {
                LoadingView(navigationPath: $navigationPath)
            } else {
                if viewModel.projects.isEmpty {
                    Text("Proyectos no encontrados")
                        .foregroundColor(customBlack)
                } else {
                    VerticalCarouselView(
                        projects: viewModel.projects,
                        navigationPath: $navigationPath
                    )
                    .navigationBarHidden(true)
                }
            }

            // Botón Atras
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
        .onAppear {
            if !viewModel.projectsLoaded {
                viewModel.loadProjects()
            }
        }
        .navigationDestination(for: Project.self) { project in
        SelectedProjectView(project: project, navigationPath: $navigationPath)
        }
    }
}